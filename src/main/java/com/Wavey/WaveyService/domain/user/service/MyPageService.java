package com.Wavey.WaveyService.domain.user.service;

import com.Wavey.WaveyService.domain.route.repository.RouteRepository;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotListResponse;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.domain.spot.service.SpotDiscoveryService;
import com.Wavey.WaveyService.domain.stamp.repository.*;
import com.Wavey.WaveyService.domain.user.dto.*;
import com.Wavey.WaveyService.domain.user.entity.*;
import com.Wavey.WaveyService.domain.user.repository.*;
import com.Wavey.WaveyService.global.exception.*;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {
    private final UserRepository users;
    private final UserSettingsRepository preferences;
    private final SavedSpotRepository saved;
    private final SpotRepository spots;
    private final SpotDiscoveryService discovery;
    private final RouteRepository routes;
    private final UserStampRepository stamps;
    private final UserBadgeRepository badges;

    public UserSettings settings(Long id) {
        return preferences
                .findByUserId(id)
                .orElseGet(() -> UserSettings.builder().userId(id).build());
    }

    public record Profile(
            Long userId,
            String name,
            String email,
            String profileImageUrl,
            String countryCode,
            long visitedSpotCount,
            long routeSpotCount,
            long savedSpotCount,
            long badgeCount) {}

    public Profile profile(Long id) {
        var u = users.findById(id).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        var s = settings(id);
        long count =
                routes.findByUserId(id).stream()
                        .flatMap(r -> r.getRouteSpots().stream())
                        .map(r -> r.getSpotId())
                        .distinct()
                        .count();
        return new Profile(
                id,
                u.getName(),
                u.getEmail(),
                s.getProfileImageUrl(),
                s.getCountryCode(),
                stamps.countByUserId(id),
                count,
                saved.countByUserId(id),
                badges.countByUserId(id));
    }

    @Transactional
    public Profile updateProfile(Long id, ProfileRequest r) {
        var u =
                users.findLockedById(id)
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        u.update(r.name().trim(), null);
        var s = settings(id);
        s.setProfileImageUrl(r.profileImageUrl());
        s.setCountryCode(r.countryCode());
        preferences.save(s);
        return profile(id);
    }

    @Transactional
    public UserSettings updateSettings(Long id, SettingsRequest r) {
        users.findLockedById(id).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        var s = settings(id);
        if (r.language() != null) s.setLanguage(r.language());
        if (r.pushEnabled() != null) s.setPushEnabled(r.pushEnabled());
        if (r.stampEnabled() != null) s.setStampEnabled(r.stampEnabled());
        if (r.routeEnabled() != null) s.setRouteEnabled(r.routeEnabled());
        if (r.noticeEnabled() != null) s.setNoticeEnabled(r.noticeEnabled());
        if (r.locationEnabled() != null) s.setLocationEnabled(r.locationEnabled());
        if (r.marketingEnabled() != null) s.setMarketingEnabled(r.marketingEnabled());
        return preferences.save(s);
    }

    @Transactional
    public void save(Long id, Long spotId, boolean enabled) {
        var spot =
                spots.findLockedById(spotId)
                        .orElseThrow(() -> new CustomException(ErrorCode.SPOT_NOT_FOUND));
        boolean exists = saved.existsByUserIdAndSpotId(id, spotId);
        if (enabled && !exists) {
            saved.save(SavedSpot.builder().userId(id).spotId(spotId).build());
            spot.changeSavedCount(1);
        }
        if (!enabled && exists) {
            saved.deleteByUserIdAndSpotId(id, spotId);
            spot.changeSavedCount(-1);
        }
    }

    public Page<SpotListResponse> saved(Long id, int page, int size, String language) {
        String lang = discovery.language(id, language);
        return saved.findByUserId(
                        id,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id")))
                .map(s -> discovery.card(discovery.require(s.getSpotId()), lang, true, null, null));
    }
}

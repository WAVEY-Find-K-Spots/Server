package com.Wavey.WaveyService.domain.stamp.service;

import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.domain.stamp.dto.StampClaimRequest;
import com.Wavey.WaveyService.domain.stamp.entity.*;
import com.Wavey.WaveyService.domain.stamp.repository.*;
import com.Wavey.WaveyService.domain.user.repository.*;
import com.Wavey.WaveyService.global.common.UiSupport;
import com.Wavey.WaveyService.global.exception.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StampService {
    private final StampRepository stamps;
    private final UserStampRepository collected;
    private final BadgeRepository badges;
    private final UserBadgeRepository awards;
    private final UserRepository users;
    private final SpotRepository spots;

    public record StampItem(
            Long stampId,
            Long spotId,
            Long regionId,
            String name,
            String description,
            String imageUrl,
            boolean acquired,
            LocalDateTime acquiredAt) {}

    public record BadgeItem(
            Long badgeId,
            String name,
            String description,
            String imageUrl,
            int requiredStamps,
            long progress,
            long remaining,
            boolean acquired,
            LocalDateTime acquiredAt) {}

    public record Book(
            long collectedCount,
            long visitedRegionCount,
            long badgeCount,
            Long remainingToNextBadge,
            List<StampItem> stamps) {}

    public record Claim(StampItem stamp, boolean newlyAcquired, List<BadgeItem> badges) {}

    private StampItem item(Stamp s, UserStamp owned, String lang) {
        var spot = spots.findById(s.getSpotId());
        return new StampItem(
                s.getId(),
                s.getSpotId(),
                spot.map(p -> p.getRegionId()).orElse(null),
                UiSupport.localized(s.getName(), s.getNameEn(), lang),
                UiSupport.localized(s.getDescription(), s.getDescriptionEn(), lang),
                s.getImageUrl(),
                owned != null,
                owned == null ? null : owned.getAcquiredAt());
    }

    public Book book(Long userId, Long regionId, String language) {
        String lang = language(userId, language);
        var owned = collected.findByUserId(userId);
        Map<Long, UserStamp> index = new HashMap<>();
        owned.forEach(s -> index.put(s.getStampId(), s));
        var items =
                stamps.findAll().stream()
                        .map(s -> item(s, index.get(s.getId()), lang))
                        .filter(s -> regionId == null || regionId.equals(s.regionId()))
                        .toList();
        Long next =
                badgeList(userId, lang).stream()
                        .filter(b -> !b.acquired())
                        .map(BadgeItem::remaining)
                        .min(Long::compareTo)
                        .orElse(null);
        return new Book(
                owned.size(),
                owned.stream().map(UserStamp::getRegionId).distinct().count(),
                awards.countByUserId(userId),
                next,
                items);
    }

    public List<BadgeItem> badgeList(Long userId, String language) {
        String lang = language(userId, language);
        var owned = collected.findByUserId(userId);
        Map<Long, UserBadge> awarded = new HashMap<>();
        awards.findByUserId(userId).forEach(b -> awarded.put(b.getBadgeId(), b));
        return badges.findAll().stream()
                .map(
                        b -> {
                            long progress = progress(b, owned);
                            var a = awarded.get(b.getId());
                            return new BadgeItem(
                                    b.getId(),
                                    UiSupport.localized(b.getName(), b.getNameEn(), lang),
                                    UiSupport.localized(
                                            b.getDescription(), b.getDescriptionEn(), lang),
                                    b.getImageUrl(),
                                    b.getRequiredStamps(),
                                    Math.min(progress, b.getRequiredStamps()),
                                    Math.max(0, b.getRequiredStamps() - progress),
                                    a != null,
                                    a == null ? null : a.getAcquiredAt());
                        })
                .toList();
    }

    private long progress(Badge b, List<UserStamp> owned) {
        return owned.stream()
                .filter(s -> b.getRegionId() == null || b.getRegionId().equals(s.getRegionId()))
                .filter(
                        s ->
                                b.getCategory() == null
                                        || spots.findById(s.getSpotId())
                                                .map(p -> p.getCategory() == b.getCategory())
                                                .orElse(false))
                .count();
    }

    @Transactional
    public Claim claim(Long spotId, Long userId, StampClaimRequest request, String language) {
        // Serialize claims for this user: unique constraints also protect duplicate stamps/badges.
        users.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        var spot = spots.findById(spotId).orElseThrow(() -> new CustomException(ErrorCode.SPOT_NOT_FOUND));
        var stamp =
                stamps.findBySpotId(spotId)
                        .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
        String lang = language(userId, language);
        var existing = collected.findByUserIdAndStampId(userId, stamp.getId());
        if (existing.isPresent())
            return new Claim(item(stamp, existing.get(), lang), false, badgeList(userId, lang));
        UiSupport.coordinates(request.latitude(), request.longitude());
        if (UiSupport.meters(
                        request.latitude(),
                        request.longitude(),
                        spot.getLatitude().doubleValue(),
                        spot.getLongitude().doubleValue())
                > stamp.getRadiusMeters()) throw new CustomException(ErrorCode.STAMP_TOO_FAR);
        var entry =
                collected.saveAndFlush(
                        UserStamp.builder()
                                .userId(userId)
                                .stampId(stamp.getId())
                                .spotId(spotId)
                                .regionId(spot.getRegionId())
                                .acquiredAt(LocalDateTime.now())
                                .build());
        var owned = collected.findByUserId(userId);
        for (var badge : badges.findAll()) {
            if (progress(badge, owned) >= badge.getRequiredStamps()
                    && !awards.existsByUserIdAndBadgeId(userId, badge.getId()))
                awards.save(
                        UserBadge.builder()
                                .userId(userId)
                                .badgeId(badge.getId())
                                .acquiredAt(LocalDateTime.now())
                                .build());
        }
        return new Claim(item(stamp, entry, lang), true, badgeList(userId, lang));
    }

    public StampItem detail(Long stampId, Long userId, String language) {
        var stamp =
                stamps.findById(stampId)
                        .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
        return item(
                stamp,
                collected.findByUserIdAndStampId(userId, stampId).orElse(null),
                language(userId, language));
    }

    private String language(Long userId, String requested) {
        return UiSupport.language(requested);
    }
}

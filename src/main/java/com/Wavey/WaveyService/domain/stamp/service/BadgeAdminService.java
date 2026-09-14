package com.Wavey.WaveyService.domain.stamp.service;

import com.Wavey.WaveyService.domain.region.repository.RegionRepository;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.domain.stamp.dto.request.BadgeCreateRequest;
import com.Wavey.WaveyService.domain.stamp.dto.request.BadgeUpdateRequest;
import com.Wavey.WaveyService.domain.stamp.dto.response.BadgeAdminResponse;
import com.Wavey.WaveyService.domain.stamp.entity.Badge;
import com.Wavey.WaveyService.domain.stamp.entity.BadgeSpot;
import com.Wavey.WaveyService.domain.stamp.repository.BadgeRepository;
import com.Wavey.WaveyService.domain.stamp.repository.BadgeSpotRepository;
import com.Wavey.WaveyService.domain.stamp.repository.UserBadgeRepository;
import com.Wavey.WaveyService.domain.upload.enums.UploadCategory;
import com.Wavey.WaveyService.domain.upload.service.UploadService;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BadgeAdminService {

    private final BadgeRepository badgeRepository;
    private final BadgeSpotRepository badgeSpotRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final RegionRepository regionRepository;
    private final SpotRepository spotRepository;
    private final UploadService uploadService;

    @Transactional
    public BadgeAdminResponse create(Long adminUserId, BadgeCreateRequest request) {
        validateRegion(request.regionId());
        List<Long> spotIds = normalizeSpotIds(request.spotIds());
        validateSpotsExist(spotIds);
        validateRequiredAgainstSpots(request.requiredStamps(), spotIds);
        String imageUrl = normalizeImageUrl(adminUserId, request.imageUrl(), false);

        Badge badge =
                badgeRepository.save(
                        Badge.builder()
                                .name(request.name())
                                .nameEn(blankToNull(request.nameEn()))
                                .description(blankToNull(request.description()))
                                .descriptionEn(blankToNull(request.descriptionEn()))
                                .imageUrl(imageUrl)
                                .requiredStamps(request.requiredStamps())
                                .regionId(request.regionId())
                                .category(request.category())
                                .build());

        replaceBadgeSpots(badge.getId(), spotIds);
        return BadgeAdminResponse.from(badge, spotIds);
    }

    public List<BadgeAdminResponse> list() {
        List<Badge> badges = badgeRepository.findAll();
        Map<Long, List<Long>> spotsByBadge = loadSpotIdsByBadge(badges);
        return badges.stream()
                .map(b -> BadgeAdminResponse.from(b, spotsByBadge.getOrDefault(b.getId(), List.of())))
                .toList();
    }

    public BadgeAdminResponse get(Long badgeId) {
        Badge badge = findBadge(badgeId);
        return BadgeAdminResponse.from(badge, badgeSpotRepository.findSpotIdsByBadgeId(badgeId));
    }

    @Transactional
    public BadgeAdminResponse update(Long adminUserId, Long badgeId, BadgeUpdateRequest request) {
        Badge badge = findBadge(badgeId);

        if (request.name() != null && !request.name().isBlank()) {
            badge.setName(request.name());
        }
        if (request.nameEn() != null) {
            badge.setNameEn(blankToNull(request.nameEn()));
        }
        if (request.description() != null) {
            badge.setDescription(blankToNull(request.description()));
        }
        if (request.descriptionEn() != null) {
            badge.setDescriptionEn(blankToNull(request.descriptionEn()));
        }
        if (request.imageUrl() != null) {
            badge.setImageUrl(normalizeImageUrl(adminUserId, request.imageUrl(), true));
        }
        if (request.requiredStamps() != null) {
            badge.setRequiredStamps(request.requiredStamps());
        }
        if (Boolean.TRUE.equals(request.clearRegionId())) {
            badge.setRegionId(null);
        } else if (request.regionId() != null) {
            validateRegion(request.regionId());
            badge.setRegionId(request.regionId());
        }
        if (Boolean.TRUE.equals(request.clearCategory())) {
            badge.setCategory(null);
        } else if (request.category() != null) {
            badge.setCategory(request.category());
        }

        List<Long> spotIds;
        if (request.spotIds() != null) {
            spotIds = normalizeSpotIds(request.spotIds());
            validateSpotsExist(spotIds);
            validateRequiredAgainstSpots(badge.getRequiredStamps(), spotIds);
            replaceBadgeSpots(badgeId, spotIds);
        } else {
            spotIds = badgeSpotRepository.findSpotIdsByBadgeId(badgeId);
            if (request.requiredStamps() != null) {
                validateRequiredAgainstSpots(request.requiredStamps(), spotIds);
            }
        }

        return BadgeAdminResponse.from(badge, spotIds);
    }

    @Transactional
    public void delete(Long badgeId) {
        Badge badge = findBadge(badgeId);
        badgeSpotRepository.deleteByBadgeId(badgeId);
        userBadgeRepository.deleteByBadgeId(badgeId);
        badgeRepository.delete(badge);
    }

    private Badge findBadge(Long badgeId) {
        return badgeRepository
                .findById(badgeId)
                .orElseThrow(() -> new CustomException(ErrorCode.BADGE_NOT_FOUND));
    }

    private void validateRegion(Long regionId) {
        if (regionId != null && !regionRepository.existsById(regionId)) {
            throw new CustomException(ErrorCode.REGION_NOT_FOUND);
        }
    }

    private void validateSpotsExist(List<Long> spotIds) {
        if (spotIds.isEmpty()) {
            return;
        }
        long found = spotRepository.findAllById(spotIds).size();
        if (found != spotIds.size()) {
            throw new CustomException(ErrorCode.SPOT_NOT_FOUND);
        }
    }

    private void validateRequiredAgainstSpots(int requiredStamps, List<Long> spotIds) {
        if (!spotIds.isEmpty() && requiredStamps > spotIds.size()) {
            throw new CustomException(ErrorCode.COMMON_INVALID_PARAMETER);
        }
    }

    private void replaceBadgeSpots(Long badgeId, List<Long> spotIds) {
        badgeSpotRepository.deleteByBadgeId(badgeId);
        if (spotIds.isEmpty()) {
            return;
        }
        List<BadgeSpot> rows = new ArrayList<>();
        for (Long spotId : spotIds) {
            rows.add(BadgeSpot.builder().badgeId(badgeId).spotId(spotId).build());
        }
        badgeSpotRepository.saveAll(rows);
    }

    private Map<Long, List<Long>> loadSpotIdsByBadge(List<Badge> badges) {
        if (badges.isEmpty()) {
            return Map.of();
        }
        List<Long> badgeIds = badges.stream().map(Badge::getId).toList();
        return badgeSpotRepository.findByBadgeIdIn(badgeIds).stream()
                .collect(
                        Collectors.groupingBy(
                                BadgeSpot::getBadgeId,
                                Collectors.mapping(BadgeSpot::getSpotId, Collectors.toList())));
    }

    private List<Long> normalizeSpotIds(List<Long> spotIds) {
        if (spotIds == null || spotIds.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(new LinkedHashSet<>(spotIds));
    }

    private String normalizeImageUrl(Long adminUserId, String imageUrl, boolean allowClear) {
        if (imageUrl == null) {
            return null;
        }
        if (imageUrl.isBlank()) {
            if (allowClear) {
                return null;
            }
            throw new CustomException(ErrorCode.UPLOAD_INVALID_FILE_URL);
        }
        uploadService.validateOwnedUrl(UploadCategory.BADGE, adminUserId, imageUrl);
        return imageUrl;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}

package com.Wavey.WaveyService.domain.stamp.service;

import com.Wavey.WaveyService.domain.notification.event.NotificationEvent;
import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.domain.stamp.dto.StampClaimRequest;
import com.Wavey.WaveyService.domain.stamp.entity.Stamp;
import com.Wavey.WaveyService.domain.stamp.entity.UserStamp;
import com.Wavey.WaveyService.domain.stamp.repository.BadgeSpotRepository;
import com.Wavey.WaveyService.domain.stamp.repository.StampRepository;
import com.Wavey.WaveyService.domain.stamp.repository.UserStampRepository;
import com.Wavey.WaveyService.domain.user.repository.UserRepository;
import com.Wavey.WaveyService.global.common.UiSupport;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StampService {

    private static final int DEFAULT_RADIUS_METERS = 150;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final StampRepository stamps;
    private final UserStampRepository collected;
    private final BadgeSpotRepository badgeSpots;
    private final UserRepository users;
    private final SpotRepository spots;
    private final UserBadgeService userBadges;
    private final ApplicationEventPublisher eventPublisher;

    @Schema(description = "스탬프 항목 (이름/이미지는 Spot에서 조회)")
    public record StampItem(
            @Schema(description = "스탬프 ID (미획득·미생성 시 null)", example = "1", nullable = true)
                    Long stampId,
            @Schema(description = "스팟 ID", example = "10") Long spotId,
            @Schema(description = "지역 ID", example = "1") Long regionId,
            @Schema(description = "이름 (language 적용)", example = "경복궁") String name,
            @Schema(description = "이미지 URL", example = "https://example.com/spots/10.png")
                    String imageUrl,
            @Schema(description = "획득 여부", example = "true") boolean acquired,
            @Schema(description = "획득 시각", example = "2026-09-14T12:00:00", nullable = true)
                    LocalDateTime acquiredAt) {}

    @Schema(description = "스탬프북 페이지")
    public record Book(
            @Schema(description = "내가 획득한 스탬프 수(전체)", example = "3") long collectedCount,
            @Schema(description = "이번 페이지 스탬프 목록") List<StampItem> stamps,
            @Schema(description = "현재 페이지(0부터)", example = "0") int page,
            @Schema(description = "전체 Spot(스탬프 후보) 수", example = "57") long totalElements,
            @Schema(description = "전체 페이지 수", example = "3") int totalPages,
            @Schema(description = "다음 페이지 여부", example = "true") boolean hasNext) {}

    @Schema(description = "스탬프 획득 결과")
    public record Claim(
            @Schema(description = "획득(또는 이미 보유)한 스탬프") StampItem stamp,
            @Schema(description = "이번 요청에서 새로 획득했는지", example = "true")
                    boolean newlyAcquired,
            @Schema(description = "갱신된 배지함 (수령은 별도 POST)")
                    UserBadgeService.BadgeCollection badges) {}

    public Book book(Long userId, Long regionId, String language, Integer page, Integer size) {
        String lang = language(language);
        int pageNo = page == null || page < 0 ? 0 : page;
        int pageSize = normalizeSize(size);

        // 정렬은 쿼리에서 처리한다: 획득한 스팟(최근 획득 순) → 미획득 스팟(id 순)
        PageRequest pageable = PageRequest.of(pageNo, pageSize);
        Page<Spot> spotPage =
                regionId == null
                        ? collected.findSpotsAcquiredFirst(userId, pageable)
                        : collected.findSpotsByRegionAcquiredFirst(userId, regionId, pageable);

        Map<Long, UserStamp> ownedBySpotId = new HashMap<>();
        collected.findByUserId(userId).forEach(us -> ownedBySpotId.put(us.getSpotId(), us));

        List<Long> spotIds = spotPage.getContent().stream().map(Spot::getId).toList();
        Map<Long, Stamp> stampBySpotId = new HashMap<>();
        if (!spotIds.isEmpty()) {
            stamps.findBySpotIdIn(spotIds).forEach(s -> stampBySpotId.put(s.getSpotId(), s));
        }

        List<StampItem> items =
                spotPage.getContent().stream()
                        .map(
                                spot ->
                                        toItem(
                                                spot,
                                                stampBySpotId.get(spot.getId()),
                                                ownedBySpotId.get(spot.getId()),
                                                lang))
                        .toList();

        return new Book(
                collected.countByUserId(userId),
                items,
                spotPage.getNumber(),
                spotPage.getTotalElements(),
                spotPage.getTotalPages(),
                spotPage.hasNext());
    }

    @Transactional
    public Claim claim(Long spotId, Long userId, StampClaimRequest request, String language) {
        users.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Spot spot =
                spots.findById(spotId)
                        .orElseThrow(() -> new CustomException(ErrorCode.SPOT_NOT_FOUND));
        String lang = language(language);

        var existing = collected.findByUserIdAndSpotId(userId, spotId);
        if (existing.isPresent()) {
            Stamp stamp =
                    stamps.findBySpotId(spotId)
                            .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
            return new Claim(
                    toItem(spot, stamp, existing.get(), lang),
                    false,
                    userBadges.collection(userId, lang));
        }

        UiSupport.coordinates(request.latitude(), request.longitude());
        Stamp stamp = getOrCreateStamp(spotId);
        if (UiSupport.meters(
                        request.latitude(),
                        request.longitude(),
                        spot.getLatitude().doubleValue(),
                        spot.getLongitude().doubleValue())
                > stamp.getRadiusMeters()) {
            throw new CustomException(ErrorCode.STAMP_TOO_FAR);
        }

        Map<Long, Long> previousBadgeProgress = userBadges.progressByBadgeId(userId);

        UserStamp entry =
                collected.saveAndFlush(
                        UserStamp.builder()
                                .userId(userId)
                                .stampId(stamp.getId())
                                .spotId(spotId)
                                .regionId(spot.getRegionId())
                                .acquiredAt(LocalDateTime.now())
                                .build());

        eventPublisher.publishEvent(
                new NotificationEvent.StampAcquired(
                        userId, stamp.getId(), spot.getNameKo(), spot.getNameEn()));

        UserBadgeService.BadgeCollection currentBadges = userBadges.collection(userId, lang);
        publishBadgeProgressEvents(userId, previousBadgeProgress, currentBadges);

        return new Claim(
                toItem(spot, stamp, entry, lang), true, currentBadges);
    }

    public StampItem detail(Long stampId, Long userId, String language) {
        Stamp stamp =
                stamps.findById(stampId)
                        .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
        Spot spot =
                spots.findById(stamp.getSpotId())
                        .orElseThrow(() -> new CustomException(ErrorCode.SPOT_NOT_FOUND));
        return toItem(
                spot,
                stamp,
                collected.findByUserIdAndStampId(userId, stampId).orElse(null),
                language(language));
    }

    @Transactional
    public void deleteBySpotId(Long spotId) {
        collected.deleteBySpotId(spotId);
        stamps.deleteBySpotId(spotId);
        badgeSpots.deleteBySpotId(spotId);
    }

    private Stamp getOrCreateStamp(Long spotId) {
        return stamps.findBySpotId(spotId)
                .orElseGet(
                        () -> {
                            try {
                                return stamps.saveAndFlush(
                                        Stamp.builder()
                                                .spotId(spotId)
                                                .radiusMeters(DEFAULT_RADIUS_METERS)
                                                .build());
                            } catch (DataIntegrityViolationException e) {
                                return stamps.findBySpotId(spotId)
                                        .orElseThrow(
                                                () ->
                                                        new CustomException(
                                                                ErrorCode.RESOURCE_NOT_FOUND));
                            }
                        });
    }

    private StampItem toItem(Spot spot, Stamp stamp, UserStamp owned, String lang) {
        return new StampItem(
                stamp == null ? null : stamp.getId(),
                spot.getId(),
                spot.getRegionId(),
                UiSupport.localized(spot.getNameKo(), spot.getNameEn(), lang),
                spot.getImageUrl(),
                owned != null,
                owned == null ? null : owned.getAcquiredAt());
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private void publishBadgeProgressEvents(
            Long userId,
            Map<Long, Long> previousProgress,
            UserBadgeService.BadgeCollection current) {
        for (UserBadgeService.BadgeItem item : badgeItemsById(current).values()) {
            long oldProgress = previousProgress.getOrDefault(item.badgeId(), 0L);
            if (item.progress() > oldProgress) {
                eventPublisher.publishEvent(
                        new NotificationEvent.BadgeProgressed(
                                userId,
                                item.badgeId(),
                                item.progress(),
                                item.requiredStamps()));
            }
        }
    }

    private Map<Long, UserBadgeService.BadgeItem> badgeItemsById(
            UserBadgeService.BadgeCollection collection) {
        Map<Long, UserBadgeService.BadgeItem> result = new HashMap<>();
        collection.acquired().forEach(item -> result.put(item.badgeId(), item));
        collection.claimable().forEach(item -> result.put(item.badgeId(), item));
        collection.inProgress().forEach(item -> result.put(item.badgeId(), item));
        return result;
    }

    private String language(String requested) {
        return UiSupport.language(requested);
    }
}

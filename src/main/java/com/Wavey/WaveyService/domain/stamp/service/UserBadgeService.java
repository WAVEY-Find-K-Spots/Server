package com.Wavey.WaveyService.domain.stamp.service;

import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.domain.stamp.entity.Badge;
import com.Wavey.WaveyService.domain.stamp.entity.UserBadge;
import com.Wavey.WaveyService.domain.stamp.entity.UserStamp;
import com.Wavey.WaveyService.domain.stamp.repository.BadgeRepository;
import com.Wavey.WaveyService.domain.stamp.repository.BadgeSpotRepository;
import com.Wavey.WaveyService.domain.stamp.repository.UserBadgeRepository;
import com.Wavey.WaveyService.domain.stamp.repository.UserStampRepository;
import com.Wavey.WaveyService.domain.upload.service.UploadService;
import com.Wavey.WaveyService.domain.user.repository.UserRepository;
import com.Wavey.WaveyService.global.common.UiSupport;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBadgeService {

    private final BadgeRepository badges;
    private final BadgeSpotRepository badgeSpots;
    private final UserBadgeRepository awards;
    private final UserStampRepository collected;
    private final UserRepository users;
    private final SpotRepository spots;
    private final UploadService uploadService;

    @Schema(description = "배지 항목")
    public record BadgeItem(
            @Schema(description = "배지 ID", example = "1") Long badgeId,
            @Schema(description = "배지 이름", example = "서울 탐험가") String name,
            @Schema(description = "배지 설명", example = "서울 스팟 5곳 방문") String description,
            @Schema(description = "이미지 URL", example = "https://example.com/badges/1.png")
                    String imageUrl,
            @Schema(description = "필요 스탬프 수 (분모)", example = "5") int requiredStamps,
            @Schema(description = "현재 진행도 (분자)", example = "3") long progress,
            @Schema(description = "획득 시각", example = "2024-03-15T12:00:00", nullable = true)
                    LocalDateTime acquiredAt) {}

    @Schema(description = "내 배지함 (획득 / 수령 가능 / 도전 중)")
    public record BadgeCollection(
            @Schema(description = "획득한 배지 수", example = "3") long acquiredCount,
            @Schema(description = "수령 가능한 배지 수", example = "1") long claimableCount,
            @Schema(description = "도전 중인 배지 수", example = "5") long inProgressCount,
            @Schema(description = "획득한 배지 목록 (최신 획득순)") List<BadgeItem> acquired,
            @Schema(description = "조건 충족·미수령 배지 (획득 버튼 대상)") List<BadgeItem> claimable,
            @Schema(description = "도전 중인 배지 목록 (진행률 높은 순)") List<BadgeItem> inProgress) {}

    @Schema(description = "배지 수령 결과")
    public record BadgeClaim(
            @Schema(description = "수령(또는 이미 보유)한 배지") BadgeItem badge,
            @Schema(description = "이번 요청에서 새로 수령했는지", example = "true")
                    boolean newlyAcquired) {}

    public BadgeCollection collection(Long userId, String language) {
        String lang = language(language);
        List<UserStamp> owned = collected.findByUserId(userId);
        Map<Long, SpotCategory> categoryBySpotId = categoryBySpotId(owned);
        List<Badge> allBadges = badges.findAll();
        Map<Long, List<Long>> setSpotsByBadgeId = loadSetSpotsByBadgeId(allBadges);

        Map<Long, UserBadge> awarded = new HashMap<>();
        awards.findByUserId(userId).forEach(b -> awarded.put(b.getBadgeId(), b));

        List<BadgeItem> acquired = new ArrayList<>();
        List<BadgeItem> claimable = new ArrayList<>();
        List<BadgeItem> inProgress = new ArrayList<>();

        for (Badge b : allBadges) {
            long raw = progress(b, owned, categoryBySpotId, setSpotsByBadgeId);
            long capped = Math.min(raw, b.getRequiredStamps());
            UserBadge award = awarded.get(b.getId());
            BadgeItem item = toItem(b, capped, award, lang);

            if (award != null) {
                acquired.add(item);
            } else if (raw >= b.getRequiredStamps()) {
                claimable.add(item);
            } else {
                inProgress.add(item);
            }
        }

        acquired.sort(
                Comparator.comparing(
                                BadgeItem::acquiredAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(BadgeItem::badgeId));
        claimable.sort(
                Comparator.comparingLong(BadgeItem::progress)
                        .reversed()
                        .thenComparing(BadgeItem::badgeId));
        inProgress.sort(
                Comparator.comparingLong(BadgeItem::progress)
                        .reversed()
                        .thenComparing(BadgeItem::badgeId));

        return new BadgeCollection(
                acquired.size(),
                claimable.size(),
                inProgress.size(),
                acquired,
                claimable,
                inProgress);
    }

    @Transactional
    public BadgeClaim claim(Long userId, Long badgeId, String language) {
        users.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Badge badge =
                badges.findById(badgeId)
                        .orElseThrow(() -> new CustomException(ErrorCode.BADGE_NOT_FOUND));
        String lang = language(language);

        var existing = awards.findByUserIdAndBadgeId(userId, badgeId);
        if (existing.isPresent()) {
            return new BadgeClaim(
                    toItem(badge, badge.getRequiredStamps(), existing.get(), lang), false);
        }

        List<UserStamp> owned = collected.findByUserId(userId);
        long raw =
                progress(
                        badge,
                        owned,
                        categoryBySpotId(owned),
                        Map.of(badgeId, badgeSpots.findSpotIdsByBadgeId(badgeId)));
        if (raw < badge.getRequiredStamps()) {
            throw new CustomException(ErrorCode.BADGE_NOT_CLAIMABLE);
        }

        try {
            UserBadge saved =
                    awards.saveAndFlush(
                            UserBadge.builder()
                                    .userId(userId)
                                    .badgeId(badgeId)
                                    .acquiredAt(LocalDateTime.now())
                                    .build());
            return new BadgeClaim(toItem(badge, badge.getRequiredStamps(), saved, lang), true);
        } catch (DataIntegrityViolationException e) {
            UserBadge again =
                    awards.findByUserIdAndBadgeId(userId, badgeId)
                            .orElseThrow(() -> new CustomException(ErrorCode.BADGE_NOT_FOUND));
            return new BadgeClaim(
                    toItem(badge, badge.getRequiredStamps(), again, lang), false);
        }
    }

    private BadgeItem toItem(Badge b, long progress, UserBadge award, String lang) {
        return new BadgeItem(
                b.getId(),
                UiSupport.localized(b.getName(), b.getNameEn(), lang),
                UiSupport.localized(b.getDescription(), b.getDescriptionEn(), lang),
                uploadService.resolveAccessUrl(b.getImageUrl()),
                b.getRequiredStamps(),
                progress,
                award == null ? null : award.getAcquiredAt());
    }

    private long progress(
            Badge b,
            List<UserStamp> owned,
            Map<Long, SpotCategory> categoryBySpotId,
            Map<Long, List<Long>> setSpotsByBadgeId) {
        List<Long> setSpotIds = setSpotsByBadgeId.getOrDefault(b.getId(), List.of());
        if (!setSpotIds.isEmpty()) {
            Set<Long> set = new HashSet<>(setSpotIds);
            return owned.stream().filter(s -> set.contains(s.getSpotId())).count();
        }
        return owned.stream()
                .filter(s -> b.getRegionId() == null || b.getRegionId().equals(s.getRegionId()))
                .filter(
                        s ->
                                b.getCategory() == null
                                        || b.getCategory()
                                                .equals(categoryBySpotId.get(s.getSpotId())))
                .count();
    }

    private Map<Long, SpotCategory> categoryBySpotId(List<UserStamp> owned) {
        List<Long> spotIds =
                owned.stream().map(UserStamp::getSpotId).distinct().toList();
        if (spotIds.isEmpty()) {
            return Map.of();
        }
        return spots.findAllById(spotIds).stream()
                .collect(Collectors.toMap(Spot::getId, Spot::getCategory, (a, b) -> a));
    }

    private Map<Long, List<Long>> loadSetSpotsByBadgeId(List<Badge> allBadges) {
        List<Long> badgeIds = allBadges.stream().map(Badge::getId).toList();
        if (badgeIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<Long>> map = new HashMap<>();
        for (var bs : badgeSpots.findByBadgeIdIn(badgeIds)) {
            map.computeIfAbsent(bs.getBadgeId(), k -> new ArrayList<>()).add(bs.getSpotId());
        }
        return map;
    }

    private String language(String requested) {
        return UiSupport.language(requested);
    }
}

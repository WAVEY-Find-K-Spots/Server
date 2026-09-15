package com.Wavey.WaveyService.domain.stamp.dto.response;

import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.stamp.entity.Badge;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "배지 관리자 응답")
public record BadgeAdminResponse(
        @Schema(description = "배지 ID", example = "1") Long badgeId,
        @Schema(description = "배지 한글명", example = "궁궐 마스터") String name,
        @Schema(description = "배지 영문명") String nameEn,
        @Schema(description = "배지 한글 설명") String description,
        @Schema(description = "배지 영문 설명") String descriptionEn,
        @Schema(description = "이미지 URL") String imageUrl,
        @Schema(description = "필요 스탬프 수", example = "4") int requiredStamps,
        @Schema(description = "지역 ID", nullable = true) Long regionId,
        @Schema(description = "스팟 카테고리", nullable = true) SpotCategory category,
        @Schema(description = "세트형 Spot ID 목록 (비어 있으면 집계형)", example = "[10, 11, 12, 13]")
                List<Long> spotIds,
        @Schema(description = "세트형 여부", example = "true") boolean setType,
        @Schema(description = "생성 시각") LocalDateTime createdAt,
        @Schema(description = "수정 시각") LocalDateTime updatedAt) {

    public static BadgeAdminResponse from(Badge badge, List<Long> spotIds) {
        List<Long> ids = spotIds == null ? List.of() : List.copyOf(spotIds);
        return new BadgeAdminResponse(
                badge.getId(),
                badge.getName(),
                badge.getNameEn(),
                badge.getDescription(),
                badge.getDescriptionEn(),
                badge.getImageUrl(),
                badge.getRequiredStamps(),
                badge.getRegionId(),
                badge.getCategory(),
                ids,
                !ids.isEmpty(),
                badge.getCreatedAt(),
                badge.getUpdatedAt());
    }
}

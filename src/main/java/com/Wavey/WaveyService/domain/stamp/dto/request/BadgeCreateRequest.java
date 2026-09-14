package com.Wavey.WaveyService.domain.stamp.dto.request;

import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(
        description =
                "배지 생성 요청. spotIds가 있으면 지정 Spot 세트형, 없으면 regionId/category 집계형.")
public record BadgeCreateRequest(
        @NotBlank
                @Size(max = 100)
                @Schema(description = "배지 한글명", example = "궁궐 마스터", requiredMode = Schema.RequiredMode.REQUIRED)
                String name,
        @Size(max = 100) @Schema(description = "배지 영문명", example = "Palace Master") String nameEn,
        @Size(max = 500)
                @Schema(description = "배지 한글 설명", example = "서울 4대 궁궐 모두 방문")
                String description,
        @Size(max = 500)
                @Schema(description = "배지 영문 설명", example = "Visit all 4 major palaces in Seoul")
                String descriptionEn,
        @Size(max = 1000)
                @Schema(
                        description =
                                "배지 이미지 URL. POST /api/v1/uploads/presigned-url (category=BADGE) fileUrl",
                        example = "https://t3.storageapi.dev/bucket/badge/1/uuid.png")
                String imageUrl,
        @NotNull
                @Min(1)
                @Schema(
                        description =
                                "필요 스탬프 수. 세트형이면 지정 Spot 중 몇 개를 모아야 하는지(보통 spotIds 개수)",
                        example = "4",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                Integer requiredStamps,
        @Positive
                @Schema(description = "지역 ID (집계형, 없으면 전체)", example = "1", nullable = true)
                Long regionId,
        @Schema(description = "스팟 카테고리 (집계형, 없으면 전체)", example = "K_HERITAGE", nullable = true)
                SpotCategory category,
        @Schema(
                        description =
                                "지정 Spot ID 목록. 있으면 세트형(이 Spot들만 카운트). 없으면 집계형.",
                        example = "[10, 11, 12, 13]")
                List<Long> spotIds) {}

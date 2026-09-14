package com.Wavey.WaveyService.domain.stamp.dto.request;

import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "배지 수정 요청 (전달한 필드만 반영)")
public record BadgeUpdateRequest(
        @Size(max = 100) @Schema(description = "배지 한글명") String name,
        @Size(max = 100) @Schema(description = "배지 영문명") String nameEn,
        @Size(max = 500) @Schema(description = "배지 한글 설명") String description,
        @Size(max = 500) @Schema(description = "배지 영문 설명") String descriptionEn,
        @Size(max = 1000)
                @Schema(description = "배지 이미지 URL. 빈 문자열이면 제거")
                String imageUrl,
        @Min(1) @Schema(description = "필요 스탬프 수") Integer requiredStamps,
        @Positive @Schema(description = "지역 ID") Long regionId,
        @Schema(description = "스팟 카테고리") SpotCategory category,
        @Schema(description = "regionId를 null로 초기화") Boolean clearRegionId,
        @Schema(description = "category를 null로 초기화") Boolean clearCategory,
        @Schema(
                        description =
                                "지정 Spot ID 목록. null이면 유지, []면 세트 해제(집계형으로), 값이 있으면 교체")
                List<Long> spotIds) {}

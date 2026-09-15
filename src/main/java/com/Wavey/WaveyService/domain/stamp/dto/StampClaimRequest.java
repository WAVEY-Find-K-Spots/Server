package com.Wavey.WaveyService.domain.stamp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;

@Schema(description = "스탬프 획득 요청 (현재 위치)")
public record StampClaimRequest(
        @NotNull
                @DecimalMin("-90")
                @DecimalMax("90")
                @Schema(description = "현재 위도", example = "37.579617", requiredMode = Schema.RequiredMode.REQUIRED)
                Double latitude,
        @NotNull
                @DecimalMin("-180")
                @DecimalMax("180")
                @Schema(description = "현재 경도", example = "126.977041", requiredMode = Schema.RequiredMode.REQUIRED)
                Double longitude) {}

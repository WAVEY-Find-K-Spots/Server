package com.Wavey.WaveyService.domain.stamp.dto;

import jakarta.validation.constraints.*;

public record StampClaimRequest(
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude) {}

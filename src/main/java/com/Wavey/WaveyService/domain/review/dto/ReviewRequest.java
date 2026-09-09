package com.Wavey.WaveyService.domain.review.dto;

import jakarta.validation.constraints.*;

public record ReviewRequest(
        @NotNull @Min(1) @Max(5) Integer rating,
        @NotBlank @Size(max = 2000) String body,
        @Pattern(regexp = "^[A-Z]{2}$") String countryCode,
        @Pattern(regexp = "^(ko|en)$") String language) {}

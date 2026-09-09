package com.Wavey.WaveyService.domain.user.dto;

import jakarta.validation.constraints.Pattern;

public record SettingsRequest(
        @Pattern(regexp = "^(ko|en)$") String language,
        Boolean pushEnabled,
        Boolean stampEnabled,
        Boolean routeEnabled,
        Boolean noticeEnabled,
        Boolean locationEnabled,
        Boolean marketingEnabled) {}

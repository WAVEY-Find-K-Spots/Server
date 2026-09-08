package com.Wavey.WaveyService.domain.user.dto;

import jakarta.validation.constraints.*;

public record ProfileRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 1000) @Pattern(regexp = "^$|^https://[^\\s]+$") String profileImageUrl,
        @Pattern(regexp = "^[A-Z]{2}$") String countryCode) {}

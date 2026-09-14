package com.Wavey.WaveyService.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record PhotoUploadUrlRequest(
        @NotBlank String contentType
) {
}

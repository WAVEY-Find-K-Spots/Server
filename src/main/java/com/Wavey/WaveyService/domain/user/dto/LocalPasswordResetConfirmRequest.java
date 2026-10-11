package com.Wavey.WaveyService.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LocalPasswordResetConfirmRequest(
        @NotBlank
        @Size(max = 256)
        String token,

        @NotBlank
        @Size(min = 8, max = 72)
        String newPassword
) {
}

package com.Wavey.WaveyService.domain.user.dto;

import java.time.Instant;

public record PhotoUploadUrlResponse(
        String uploadUrl,
        String photoUrl,
        Instant expiresAt
) {
}

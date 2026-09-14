package com.Wavey.WaveyService.domain.upload.dto;

import java.time.Instant;

public record PresignedUploadResponse(
        String uploadUrl,
        String fileUrl,
        Instant expiresAt
) {
}

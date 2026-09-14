package com.Wavey.WaveyService.domain.upload.dto;

import com.Wavey.WaveyService.domain.upload.enums.UploadCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PresignedUploadRequest(
        @NotNull UploadCategory category,
        @NotBlank String contentType
) {
}

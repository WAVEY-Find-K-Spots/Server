package com.Wavey.WaveyService.domain.docent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "원본 이미지 기준 OCR 좌표")
public record OcrPoint(
        @Schema(description = "원본 이미지 기준 X 좌표", example = "120")
        int x,
        @Schema(description = "원본 이미지 기준 Y 좌표", example = "80")
        int y
) {
}

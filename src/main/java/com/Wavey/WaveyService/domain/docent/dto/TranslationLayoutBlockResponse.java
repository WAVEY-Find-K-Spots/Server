package com.Wavey.WaveyService.domain.docent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "프론트엔드 번역 이미지 합성용 OCR 문단 레이아웃")
public record TranslationLayoutBlockResponse(
        @Schema(description = "해당 영역에서 인식한 원문", example = "김치찌개")
        String originalText,
        @Schema(description = "해당 영역의 영문 번역", example = "Kimchi Stew")
        String translatedText,
        @Schema(description = "좌상단부터 시계 방향으로 정렬된 원본 이미지 좌표")
        List<OcrPoint> polygon,
        @Schema(description = "Google Vision OCR 신뢰도", example = "0.97")
        float confidence
) {
    public TranslationLayoutBlockResponse {
        polygon = polygon == null ? List.of() : List.copyOf(polygon);
    }

    public static TranslationLayoutBlockResponse of(
            OcrTextBlock block,
            String translatedText
    ) {
        return new TranslationLayoutBlockResponse(
                block.text(),
                translatedText,
                block.polygon(),
                block.confidence()
        );
    }
}

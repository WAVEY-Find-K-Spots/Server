package com.Wavey.WaveyService.domain.docent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "OCR 번역 및 선택형 번역 이미지 합성 데이터")
public record TranslationResponse(
        @Schema(description = "정규화된 전체 OCR 원문")
        String originalText,
        @Schema(description = "전체 영문 번역 결과")
        String translatedText,
        @Schema(description = "원문에서 매칭된 문화 용어 설명")
        List<CulturalTermResponse> terms,
        @Schema(description = "번역 이미지 합성용 문단별 좌표와 번역. 합성하지 않을 경우 무시 가능")
        List<TranslationLayoutBlockResponse> layoutBlocks
) {
    public TranslationResponse {
        terms = terms == null ? List.of() : List.copyOf(terms);
        layoutBlocks = layoutBlocks == null ? List.of() : List.copyOf(layoutBlocks);
    }

    public TranslationResponse(
            String originalText,
            String translatedText,
            List<CulturalTermResponse> terms
    ) {
        this(originalText, translatedText, terms, List.of());
    }
}

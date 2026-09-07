package com.Wavey.WaveyService.domain.docent.dto;

import java.util.List;

public record TranslationResponse(
        String originalText,
        String translatedText,
        List<CulturalTermResponse> terms
) {
}

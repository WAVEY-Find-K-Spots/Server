package com.Wavey.WaveyService.domain.docent.dto;

import com.Wavey.WaveyService.domain.docent.model.CulturalTerm;

public record CulturalTermResponse(
        String original, String translatedName, String koreanDescription, String description,
        String category, String domain, String source, String translationSource,
        String descriptionSource, boolean ambiguous
) {
    public static CulturalTermResponse from(CulturalTerm term) {
        return new CulturalTermResponse(
                term.korean(), term.english(), term.koreanDescription(), term.englishDescription(),
                term.category(), term.domain(), term.source(), term.translationSource(),
                term.descriptionSource(), term.ambiguous()
        );
    }
}

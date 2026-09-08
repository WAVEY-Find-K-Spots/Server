package com.Wavey.WaveyService.domain.docent.model;

public record CulturalTerm(
        String korean, String english, String koreanDescription, String englishDescription,
        String category, String source, String domain, String translationSource,
        String descriptionSource, boolean ambiguous
) {
    public CulturalTerm(String korean, String english, String englishDescription, String category, String source) {
        this(korean, english, null, englishDescription, category, source,
                "CULTURAL_TERM", "OFFICIAL_DATASET", "OFFICIAL_DATASET", false);
    }
}

package com.Wavey.WaveyService.domain.docent.model;

public record HeritageRecord(
        String id, String koreanName, String officialEnglishName, String displayEnglishName,
        String englishNameSource, String regionAddress, String detailedAddress,
        String canonicalRegion1, String canonicalRegion2, String designationType,
        String koreanDescription, String englishDescription, String descriptionSource,
        boolean translationRequired
) {
}

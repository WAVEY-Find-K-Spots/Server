package com.Wavey.WaveyService.domain.docent.dto;

import com.Wavey.WaveyService.domain.docent.model.HeritageRecord;

public record HeritageResponse(
        String id, String koreanName, String englishName, String englishNameSource,
        String address, String detailedAddress, String designationType,
        String koreanDescription, String englishDescription, String descriptionSource,
        boolean translationRequired
) {
    public static HeritageResponse from(HeritageRecord record) {
        return from(
                record,
                record.displayEnglishName(),
                record.englishNameSource(),
                record.englishDescription(),
                record.descriptionSource(),
                record.translationRequired()
        );
    }

    public static HeritageResponse from(
            HeritageRecord record,
            String englishName,
            String englishNameSource,
            String englishDescription,
            String descriptionSource,
            boolean translationRequired
    ) {
        return new HeritageResponse(
                record.id(), record.koreanName(), englishName, englishNameSource,
                record.regionAddress(), record.detailedAddress(), record.designationType(),
                record.koreanDescription(), englishDescription, descriptionSource,
                translationRequired
        );
    }
}
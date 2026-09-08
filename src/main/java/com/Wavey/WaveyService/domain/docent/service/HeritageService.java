package com.Wavey.WaveyService.domain.docent.service;

import com.Wavey.WaveyService.domain.docent.client.GeocodingClient;
import com.Wavey.WaveyService.domain.docent.client.TranslationClient;
import com.Wavey.WaveyService.domain.docent.dto.HeritageResponse;
import com.Wavey.WaveyService.domain.docent.model.AdministrativeArea;
import com.Wavey.WaveyService.domain.docent.model.HeritageRecord;
import com.Wavey.WaveyService.domain.docent.repository.HeritageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HeritageService {

    private final HeritageRepository heritageRepository;
    private final GeocodingClient geocodingClient;
    private final TranslationClient translationClient;

    public List<HeritageResponse> process(
            List<String> rawLandmarkCandidates, String rawText, Double latitude, Double longitude
    ) {
        AdministrativeArea area = resolveArea(latitude, longitude);
        String region1 = area == null ? null : area.region1();
        String region2 = area == null ? null : area.region2();

        List<HeritageRecord> candidates = heritageRepository.findCandidates(
                rawText, rawLandmarkCandidates, region1, region2
        );
        if (candidates.isEmpty() && region1 != null && region2 != null && !region2.isBlank()) {
            candidates = heritageRepository.findCandidates(
                    rawText, rawLandmarkCandidates, region1, null
            );
        }
        return enrichEnglishFields(candidates);
    }

    private AdministrativeArea resolveArea(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        return geocodingClient.reverseGeocode(latitude, longitude).orElse(null);
    }

    private List<HeritageResponse> enrichEnglishFields(List<HeritageRecord> records) {
        List<String> translationInputs = new ArrayList<>();
        for (HeritageRecord record : records) {
            if (isBlank(record.displayEnglishName())) {
                translationInputs.add(record.koreanName());
            }
            if (isBlank(record.englishDescription()) && !isBlank(record.koreanDescription())) {
                translationInputs.add(record.koreanDescription());
            }
        }

        Iterator<String> translations = translationInputs.isEmpty()
                ? List.<String>of().iterator()
                : translationClient.translateKoreanToEnglish(translationInputs, true).iterator();

        List<HeritageResponse> responses = new ArrayList<>();
        for (HeritageRecord record : records) {
            String englishName = record.displayEnglishName();
            String nameSource = record.englishNameSource();
            if (isBlank(englishName)) {
                englishName = translations.next();
                nameSource = "GOOGLE_TRANSLATION";
            }

            String englishDescription = record.englishDescription();
            String descriptionSource = record.descriptionSource();
            if (isBlank(englishDescription) && !isBlank(record.koreanDescription())) {
                englishDescription = translations.next();
                descriptionSource = "GOOGLE_TRANSLATION_OF_TEMPORARY_DESCRIPTION";
            }

            responses.add(HeritageResponse.from(
                    record,
                    englishName,
                    nameSource,
                    englishDescription,
                    descriptionSource,
                    isBlank(englishName)
            ));
        }
        return List.copyOf(responses);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
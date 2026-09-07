package com.Wavey.WaveyService.domain.docent.service;

import com.Wavey.WaveyService.domain.docent.client.GeocodingClient;
import com.Wavey.WaveyService.domain.docent.client.TranslationClient;
import com.Wavey.WaveyService.domain.docent.dto.HeritageResponse;
import com.Wavey.WaveyService.domain.docent.dto.WebDetectionRawData;
import com.Wavey.WaveyService.domain.docent.dto.WebDetectionResponse;
import com.Wavey.WaveyService.domain.docent.model.AdministrativeArea;
import com.Wavey.WaveyService.domain.docent.model.HeritageRecord;
import com.Wavey.WaveyService.domain.docent.repository.HeritageRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InternalVisionServicesTest {

    private final HeritageRecord sungnyemun = new HeritageRecord(
            "KHS-00001",
            "서울 숭례문",
            "Sungnyemun Gate, Seoul",
            "Sungnyemun Gate, Seoul",
            "OFFICIAL_DATASET",
            "서울특별시 중구",
            "서울 중구 세종대로 40",
            "서울특별시",
            "중구",
            "국보",
            "국가유산 종목: 국보",
            null,
            "TEMPORARY_DESIGNATION",
            false
    );
    private final HeritageRepository heritageRepository =
            (rawText, visionCandidates, region1, region2) -> {
                assertThat(region1).isEqualTo("서울특별시");
                assertThat(region2).isEqualTo("중구");
                return List.of(sungnyemun);
            };
    private final GeocodingClient geocodingClient = (latitude, longitude) -> Optional.of(
            new AdministrativeArea("서울특별시", "중구", "대한민국 서울특별시 중구")
    );
    private final TranslationClient translationClient =
            (text, glossaryRequired) -> "National heritage category: National Treasure";
    private final HeritageService heritageService = new HeritageService(
            heritageRepository,
            geocodingClient,
            translationClient
    );
    private final WebSearchService webSearchService = new WebSearchService();

    @Test
    void 내부_서비스는_이미지_없이_raw_데이터로_테스트할_수_있다() {
        List<HeritageResponse> landmarks = heritageService.process(
                List.of("Sungnyemun Gate, Seoul"),
                "서울 숭례문",
                37.5599,
                126.9753
        );
        WebDetectionRawData rawWebData = new WebDetectionRawData(
                List.of("Gwangjang Market"),
                List.of("", "Traditional market"),
                List.of(new WebDetectionRawData.WebPage("광장시장", "https://example.com"))
        );
        WebDetectionResponse webResult = webSearchService.process(rawWebData);

        assertThat(landmarks).extracting(HeritageResponse::id).containsExactly("KHS-00001");
        assertThat(landmarks.getFirst().englishNameSource()).isEqualTo("OFFICIAL_DATASET");
        assertThat(landmarks.getFirst().englishDescription())
                .isEqualTo("National heritage category: National Treasure");
        assertThat(landmarks.getFirst().descriptionSource())
                .isEqualTo("GOOGLE_TRANSLATION_OF_TEMPORARY_DESCRIPTION");
        assertThat(webResult.getWebEntities()).containsExactly("Traditional market");
        assertThat(webResult.getPagesWithImages()).hasSize(1);
    }
}
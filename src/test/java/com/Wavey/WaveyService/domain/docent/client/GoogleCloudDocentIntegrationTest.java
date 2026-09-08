package com.Wavey.WaveyService.domain.docent.client;

import com.Wavey.WaveyService.domain.docent.model.AdministrativeArea;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_GOOGLE_INTEGRATION_TESTS", matches = "true")
class GoogleCloudDocentIntegrationTest {

    @Autowired
    private TranslationClient translationClient;

    @Autowired
    private GeocodingClient geocodingClient;

    @Test
    void 실제_Google_Glossary와_Reverse_Geocoding을_호출한다() {
        String translated = translationClient.translateKoreanToEnglish(
                "경복궁에서 육회를 먹었습니다.",
                true
        );
        AdministrativeArea area = geocodingClient.reverseGeocode(
                37.579617,
                126.977041
        ).orElseThrow();

        assertThat(translated)
                .contains("Gyeongbokgung Palace")
                .contains("Beef Tartare")
                .doesNotContain("Palace .");
        assertThat(area.region1()).isEqualTo("서울특별시");
        assertThat(area.region2()).isEqualTo("종로구");
    }
}
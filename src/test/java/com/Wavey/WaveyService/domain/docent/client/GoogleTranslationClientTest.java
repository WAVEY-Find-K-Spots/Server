package com.Wavey.WaveyService.domain.docent.client;

import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GoogleTranslationClientTest {

    @Test
    void Glossary_결과를_우선하고_구두점_앞_공백을_제거한다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GoogleTranslationClient client = client(builder, "wavey-ko-en-tourism");

        server.expect(requestTo(
                        "https://translation.googleapis.com/v3/projects/test-project/locations/us-central1:translateText"
                ))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andExpect(content().json("""
                        {
                          "sourceLanguageCode": "ko",
                          "targetLanguageCode": "en",
                          "contents": ["경복궁에서 육회를 먹었습니다."],
                          "mimeType": "text/plain",
                          "glossaryConfig": {
                            "glossary": "projects/test-project/locations/us-central1/glossaries/wavey-ko-en-tourism",
                            "ignoreCase": true
                          }
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "translations": [
                            {"translatedText": "I ate raw beef at Gyeongbokgung Palace."}
                          ],
                          "glossaryTranslations": [
                            {"translatedText": "I ate Beef Tartare at Gyeongbokgung Palace ."}
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.translateKoreanToEnglish(
                "경복궁에서 육회를 먹었습니다.", true
        )).isEqualTo("I ate Beef Tartare at Gyeongbokgung Palace.");
        server.verify();
    }

    @Test
    void 여러_문화유산_필드를_한번에_번역한다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GoogleTranslationClient client = client(builder, "wavey-ko-en-tourism");

        server.expect(requestTo(
                        "https://translation.googleapis.com/v3/projects/test-project/locations/us-central1:translateText"
                ))
                .andRespond(withSuccess("""
                        {
                          "glossaryTranslations": [
                            {"translatedText": "Sungnyemun Gate"},
                            {"translatedText": "National heritage category: National Treasure"}
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.translateKoreanToEnglish(
                List.of("숭례문", "국가유산 종목: 국보"), true
        )).containsExactly(
                "Sungnyemun Gate",
                "National heritage category: National Treasure"
        );
        server.verify();
    }

    @Test
    void Glossary_ID가_없으면_외부_요청_전에_설정_오류를_반환한다() {
        GoogleTranslationClient client = client(RestClient.builder(), "");

        assertThatThrownBy(() -> client.translateKoreanToEnglish("육회", true))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GOOGLE_TRANSLATION_GLOSSARY_CONFIGURATION_MISSING);
    }

    private GoogleTranslationClient client(RestClient.Builder builder, String glossaryId) {
        CredentialsProvider credentialsProvider = () -> GoogleCredentials.create(
                new AccessToken(
                        "test-token",
                        new Date(System.currentTimeMillis() + 3_600_000)
                )
        );
        return new GoogleTranslationClient(
                builder,
                credentialsProvider,
                "https://translation.googleapis.com",
                "test-project",
                "us-central1",
                glossaryId
        );
    }
}
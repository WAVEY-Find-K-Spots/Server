package com.Wavey.WaveyService.domain.docent.client;

import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Google Cloud Translation Advanced v3와 등록된 관광 Glossary를 호출합니다. */
@Slf4j
@Component
public class GoogleTranslationClient implements TranslationClient {

    private static final String CLOUD_PLATFORM_SCOPE =
            "https://www.googleapis.com/auth/cloud-platform";

    private final RestClient restClient;
    private final CredentialsProvider credentialsProvider;
    private final String projectId;
    private final String location;
    private final String glossaryId;

    public GoogleTranslationClient(
            RestClient.Builder restClientBuilder,
            CredentialsProvider credentialsProvider,
            @Value("${translation.google.base-url:https://translation.googleapis.com}") String baseUrl,
            @Value("${spring.cloud.gcp.project-id:}") String projectId,
            @Value("${translation.google.location:us-central1}") String location,
            @Value("${translation.google.glossary-id:}") String glossaryId
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.credentialsProvider = credentialsProvider;
        this.projectId = projectId;
        this.location = location;
        this.glossaryId = glossaryId;
    }

    @Override
    public String translateKoreanToEnglish(String text, boolean glossaryRequired) {
        return translateKoreanToEnglish(List.of(text), glossaryRequired).getFirst();
    }

    @Override
    public List<String> translateKoreanToEnglish(List<String> texts, boolean glossaryRequired) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }
        validateConfiguration(glossaryRequired);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("sourceLanguageCode", "ko");
        request.put("targetLanguageCode", "en");
        request.put("contents", texts);
        request.put("mimeType", "text/plain");
        if (glossaryRequired) {
            request.put("glossaryConfig", Map.of(
                    "glossary", glossaryName(),
                    "ignoreCase", true
            ));
        }

        try {
            GoogleTranslationResponse response = restClient.post()
                    .uri("/v3/projects/{projectId}/locations/{location}:translateText", projectId, location)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> {
                        headers.setBearerAuth(accessToken());
                    })
                    .body(request)
                    .retrieve()
                    .body(GoogleTranslationResponse.class);

            List<TranslationItem> selected = selectTranslations(response, glossaryRequired);
            if (selected.size() != texts.size()) {
                throw new CustomException(ErrorCode.GOOGLE_TRANSLATION_FAILED);
            }
            return selected.stream()
                    .map(TranslationItem::translatedText)
                    .map(this::normalizePunctuation)
                    .toList();
        } catch (CustomException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            log.warn("Google Translation 응답 오류: status={}", exception.getStatusCode().value());
            if (exception.getStatusCode().value() == 429) {
                throw new CustomException(ErrorCode.GOOGLE_TRANSLATION_QUOTA_EXHAUSTED);
            }
            throw new CustomException(ErrorCode.GOOGLE_TRANSLATION_FAILED);
        } catch (RestClientException exception) {
            throw new CustomException(ErrorCode.GOOGLE_TRANSLATION_FAILED);
        }
    }

    private List<TranslationItem> selectTranslations(
            GoogleTranslationResponse response,
            boolean glossaryRequired
    ) {
        if (response == null) {
            return List.of();
        }
        if (glossaryRequired
                && response.glossaryTranslations() != null
                && !response.glossaryTranslations().isEmpty()) {
            return response.glossaryTranslations();
        }
        return response.translations() == null ? List.of() : response.translations();
    }

    private String accessToken() {
        try {
            Credentials rawCredentials = credentialsProvider.getCredentials();
            if (!(rawCredentials instanceof GoogleCredentials googleCredentials)) {
                throw new CustomException(ErrorCode.GOOGLE_TRANSLATION_CONFIGURATION_MISSING);
            }
            if (googleCredentials.createScopedRequired()) {
                googleCredentials = googleCredentials.createScoped(CLOUD_PLATFORM_SCOPE);
            }
            googleCredentials.refreshIfExpired();
            if (googleCredentials.getAccessToken() == null) {
                throw new CustomException(ErrorCode.GOOGLE_TRANSLATION_CONFIGURATION_MISSING);
            }
            return googleCredentials.getAccessToken().getTokenValue();
        } catch (IOException exception) {
            throw new CustomException(ErrorCode.GOOGLE_TRANSLATION_CONFIGURATION_MISSING);
        }
    }

    private void validateConfiguration(boolean glossaryRequired) {
        if (projectId == null || projectId.isBlank()
                || location == null || location.isBlank()) {
            throw new CustomException(ErrorCode.GOOGLE_TRANSLATION_CONFIGURATION_MISSING);
        }
        if (glossaryRequired && (glossaryId == null || glossaryId.isBlank())) {
            throw new CustomException(ErrorCode.GOOGLE_TRANSLATION_GLOSSARY_CONFIGURATION_MISSING);
        }
    }

    private String glossaryName() {
        return "projects/%s/locations/%s/glossaries/%s"
                .formatted(projectId, location, glossaryId);
    }

    private String normalizePunctuation(String text) {
        if (text == null) {
            throw new CustomException(ErrorCode.GOOGLE_TRANSLATION_FAILED);
        }
        return text.replaceAll("\\s+([.,!?;:])", "$1").trim();
    }

    private record GoogleTranslationResponse(
            List<TranslationItem> translations,
            List<TranslationItem> glossaryTranslations
    ) {
    }

    private record TranslationItem(String translatedText) {
    }
}
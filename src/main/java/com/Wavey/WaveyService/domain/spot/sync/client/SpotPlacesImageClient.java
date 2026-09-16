package com.Wavey.WaveyService.domain.spot.sync.client;

import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Google Places API(New)로 장소를 검색해 대표 사진 1장을 가져오는 클라이언트.
 * <ul>
 *     <li>{@code places:searchText} — 장소명+주소로 검색해 첫 결과의 photo resource name을 얻는다.</li>
 *     <li>Photo Media ({@code skipHttpRedirect=true}) — photo resource name으로 실제 다운로드 가능한 이미지 URL을 얻는다.</li>
 * </ul>
 * 두 번째 단계에서 받은 URL은 우리 API 키가 없어도 당분간 접근 가능하지만, 영구 보관을 위해
 * 호출한 쪽에서 바이트를 내려받아 우리 S3에 재업로드해야 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpotPlacesImageClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${spot.sync.google-places.base-url:https://places.googleapis.com}")
    private String baseUrl;

    @Value("${spot.sync.google-places.api-key:}")
    private String apiKey;

    @Value("${spot.sync.google-places.photo-max-width-px:800}")
    private int photoMaxWidthPx;

    public record PhotoResult(byte[] bytes, String contentType) {}

    /**
     * 장소명 + 주소로 검색해 대표 사진 바이트를 가져온다. 검색 결과가 없거나 사진이 없으면 빈 값.
     */
    public Optional<PhotoResult> findPhoto(String nameKo, String addressKo) {
        validateApiKey();
        String photoName = searchPhotoName(nameKo, addressKo);
        if (photoName == null) {
            return Optional.empty();
        }
        return fetchPhotoBytes(photoName);
    }

    private String searchPhotoName(String nameKo, String addressKo) {
        String query = (nameKo == null ? "" : nameKo) + " " + (addressKo == null ? "" : addressKo);
        String body = """
                {"textQuery":"%s"}
                """.formatted(query.trim().replace("\"", "\\\""));

        try {
            JsonNode response = restClientBuilder.build()
                    .post()
                    .uri(baseUrl + "/v1/places:searchText")
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", "places.id,places.photos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode places = response == null ? null : response.path("places");
            if (places == null || !places.isArray() || places.isEmpty()) {
                return null;
            }
            JsonNode photos = places.get(0).path("photos");
            if (!photos.isArray() || photos.isEmpty()) {
                return null;
            }
            String name = photos.get(0).path("name").asText(null);
            return StringUtils.hasText(name) ? name : null;
        } catch (RestClientResponseException e) {
            log.warn("Google Places text search failed. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.SPOT_EXTERNAL_API_REQUEST_FAILED);
        } catch (RestClientException e) {
            log.warn("Google Places text search failed.", e);
            throw new CustomException(ErrorCode.SPOT_EXTERNAL_API_REQUEST_FAILED);
        }
    }

    private Optional<PhotoResult> fetchPhotoBytes(String photoName) {
        try {
            JsonNode mediaResponse = restClientBuilder.build()
                    .get()
                    .uri(baseUrl + "/v1/" + photoName + "/media?maxWidthPx=" + photoMaxWidthPx
                            + "&skipHttpRedirect=true&key=" + apiKey)
                    .retrieve()
                    .body(JsonNode.class);

            String photoUri = mediaResponse == null ? null : mediaResponse.path("photoUri").asText(null);
            if (!StringUtils.hasText(photoUri)) {
                return Optional.empty();
            }

            byte[] bytes = restClientBuilder.build()
                    .get()
                    .uri(photoUri)
                    .retrieve()
                    .body(byte[].class);

            if (bytes == null || bytes.length == 0) {
                return Optional.empty();
            }
            return Optional.of(new PhotoResult(bytes, "image/jpeg"));
        } catch (RestClientResponseException e) {
            log.warn("Google Places photo fetch failed. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (RestClientException e) {
            log.warn("Google Places photo fetch failed.", e);
            return Optional.empty();
        }
    }

    private void validateApiKey() {
        if (!StringUtils.hasText(apiKey)) {
            throw new CustomException(ErrorCode.SPOT_EXTERNAL_API_KEY_MISSING);
        }
    }
}

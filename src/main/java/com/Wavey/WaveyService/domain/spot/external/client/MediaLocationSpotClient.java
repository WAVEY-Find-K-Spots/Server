package com.Wavey.WaveyService.domain.spot.external.client;

import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.enums.SpotSourceType;
import com.Wavey.WaveyService.domain.spot.external.dto.ExternalSpotPage;
import com.Wavey.WaveyService.domain.spot.external.dto.ExternalSpotPayload;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaLocationSpotClient {

    private static final String FIELD_MEDIA_TYPE = "\uBBF8\uB514\uC5B4\uD0C0\uC785";
    private static final String FIELD_PLACE_NAME = "\uC7A5\uC18C\uBA85";
    private static final String FIELD_SEQUENCE = "\uC5F0\uBC88";
    private static final String FIELD_LATITUDE = "\uC704\uB3C4";
    private static final String FIELD_LONGITUDE = "\uACBD\uB3C4";
    private static final String FIELD_ADDRESS = "\uC8FC\uC18C";
    private static final String FIELD_OPENING_HOURS = "\uC601\uC5C5\uC2DC\uAC04";
    private static final String FIELD_CLOSED_DAYS = "\uD734\uBB34\uC77C";
    private static final String FIELD_TEL = "\uC804\uD654\uBC88\uD638";
    private static final String FIELD_TITLE = "\uC81C\uBAA9";
    private static final String FIELD_DESCRIPTION = "\uC7A5\uC18C\uC124\uBA85";
    private static final String NO_INFO = "\uC815\uBCF4\uC5C6\uC74C";

    private static final Map<SpotCategory, String> CATEGORY_TO_MEDIA_TYPE = Map.of(
            SpotCategory.K_DRAMA, "drama",
            SpotCategory.K_POP, "artist",
            SpotCategory.K_MOVIE, "movie"
    );

    private final RestClient.Builder restClientBuilder;

    @Value("${external-api.media-location.base-url}")
    private String baseUrl;

    @Value("${external-api.service-key}")
    private String serviceKey;

    public List<ExternalSpotPayload> fetchMediaLocationSpots(SpotCategory category, int page, int perPage) {
        return fetchMediaLocationSpotPage(category, page, perPage).getItems();
    }

    public ExternalSpotPage fetchMediaLocationSpotPage(SpotCategory category, int page, int perPage) {
        validateServiceKey();
        validateCategory(category);

        try {
            JsonNode response = restClientBuilder.build()
                    .get()
                    .uri(buildMediaLocationUri(category, page, perPage))
                    .retrieve()
                    .body(JsonNode.class);

            List<ExternalSpotPayload> items = extractData(response)
                    .stream()
                    .map(this::toMediaLocationPayload)
                    .filter(Objects::nonNull)
                    .filter(payload -> category == null || payload.getCategory() == category)
                    .toList();

            return ExternalSpotPage.builder()
                    .items(items)
                    .page(page)
                    .size(perPage)
                    .totalCount(totalCount(response))
                    .build();
        } catch (RestClientResponseException e) {
            log.warn("Media location API request failed. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.SPOT_EXTERNAL_API_REQUEST_FAILED);
        } catch (RestClientException e) {
            log.warn("Media location API request failed.", e);
            throw new CustomException(ErrorCode.SPOT_EXTERNAL_API_REQUEST_FAILED);
        }
    }

    private URI buildMediaLocationUri(SpotCategory category, int page, int perPage) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("page", page)
                .queryParam("perPage", perPage)
                .queryParam("returnType", "JSON");

        if (category != null) {
            uriBuilder.queryParam("cond[" + FIELD_MEDIA_TYPE + "::EQ]", CATEGORY_TO_MEDIA_TYPE.get(category));
        }

        String uriWithoutServiceKey = uriBuilder
                .encode(StandardCharsets.UTF_8)
                .build(false)
                .toUriString();

        return URI.create(uriWithoutServiceKey + "&serviceKey=" + encodedServiceKey());
    }

    private List<JsonNode> extractData(JsonNode response) {
        JsonNode dataNode = response == null ? null : response.path("data");
        if (dataNode == null || !dataNode.isArray()) {
            return List.of();
        }

        List<JsonNode> data = new ArrayList<>();
        dataNode.forEach(data::add);
        return data;
    }

    private int totalCount(JsonNode response) {
        if (response == null) {
            return 0;
        }
        return response.path("totalCount").asInt(0);
    }

    private ExternalSpotPayload toMediaLocationPayload(JsonNode item) {
        SpotCategory category = toCategory(text(item, FIELD_MEDIA_TYPE));
        String name = text(item, FIELD_PLACE_NAME);
        String externalContentId = text(item, FIELD_SEQUENCE);
        BigDecimal latitude = decimal(item, FIELD_LATITUDE);
        BigDecimal longitude = decimal(item, FIELD_LONGITUDE);

        if (category == null
                || !StringUtils.hasText(name)
                || !StringUtils.hasText(externalContentId)
                || latitude == null
                || longitude == null) {
            return null;
        }

        return ExternalSpotPayload.builder()
                .name(name)
                .category(category)
                .address(text(item, FIELD_ADDRESS))
                .latitude(latitude)
                .longitude(longitude)
                .description(description(item))
                .openingHours(text(item, FIELD_OPENING_HOURS))
                .closedDays(text(item, FIELD_CLOSED_DAYS))
                .tel(text(item, FIELD_TEL))
                .sourceType(SpotSourceType.MEDIA_LOCATION_DATA)
                .externalContentId(externalContentId)
                .build();
    }

    private SpotCategory toCategory(String mediaType) {
        if (!StringUtils.hasText(mediaType)) {
            return null;
        }

        String normalizedMediaType = mediaType.trim().toLowerCase(Locale.ROOT);
        return CATEGORY_TO_MEDIA_TYPE.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(normalizedMediaType))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private String description(JsonNode item) {
        String title = text(item, FIELD_TITLE);
        String placeDescription = text(item, FIELD_DESCRIPTION);

        if (!StringUtils.hasText(title)) {
            return placeDescription;
        }
        if (!StringUtils.hasText(placeDescription)) {
            return title;
        }
        return "[" + title + "] " + placeDescription;
    }

    private String text(JsonNode node, String fieldName) {
        if (node == null || node.path(fieldName).isMissingNode() || node.path(fieldName).isNull()) {
            return null;
        }

        String value = node.path(fieldName).asText();
        if (!StringUtils.hasText(value) || NO_INFO.equals(value.trim())) {
            return null;
        }
        return value.trim();
    }

    private BigDecimal decimal(JsonNode node, String fieldName) {
        String value = text(node, fieldName);
        if (!StringUtils.hasText(value)) {
            return null;
        }

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void validateServiceKey() {
        if (!StringUtils.hasText(serviceKey)) {
            throw new CustomException(ErrorCode.SPOT_EXTERNAL_API_KEY_MISSING);
        }
    }

    private void validateCategory(SpotCategory category) {
        if (category != null && !CATEGORY_TO_MEDIA_TYPE.containsKey(category)) {
            throw new CustomException(ErrorCode.COMMON_INVALID_PARAMETER);
        }
    }

    private String encodedServiceKey() {
        String trimmedServiceKey = serviceKey.trim();
        if (trimmedServiceKey.contains("%")) {
            return trimmedServiceKey;
        }
        return UriUtils.encodeQueryParam(trimmedServiceKey, StandardCharsets.UTF_8);
    }
}

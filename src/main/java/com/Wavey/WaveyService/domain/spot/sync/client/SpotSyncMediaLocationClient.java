package com.Wavey.WaveyService.domain.spot.sync.client;

import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.enums.ExternalSource;
import com.Wavey.WaveyService.domain.spot.sync.dto.SpotSyncPage;
import com.Wavey.WaveyService.domain.spot.sync.dto.SpotSyncPayload;
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
import java.util.Set;
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
public class SpotSyncMediaLocationClient {

    private static final String MEDIA_TYPE = "미디어타입";
    private static final String PLACE_NAME = "장소명";
    private static final String PLACE_TYPE = "장소타입";
    private static final String SEQUENCE = "연번";
    private static final String TITLE = "제목";
    private static final String BREAK_TIME = "브레이크타임";
    private static final String LATITUDE = "위도";
    private static final String LONGITUDE = "경도";
    private static final String ADDRESS = "주소";
    private static final String OPENING_HOURS = "영업시간";
    private static final String CLOSED_DAYS = "휴무일";
    private static final String TEL = "전화번호";
    private static final String DESCRIPTION = "장소설명";
    private static final Map<SpotCategory, Set<String>> CATEGORY_TYPES = Map.of(
            SpotCategory.K_DRAMA, Set.of("drama"),
            SpotCategory.K_POP, Set.of("artist", "show"),
            SpotCategory.K_MOVIE, Set.of("movie")
    );

    private final RestClient.Builder restClientBuilder;

    @Value("${external-api.media-location.base-url}")
    private String baseUrl;

    @Value("${external-api.service-key:}")
    private String serviceKey;

    public SpotSyncPage fetch(SpotCategory category, int page, int perPage) {
        validateServiceKey();
        if (category != null && !CATEGORY_TYPES.containsKey(category)) {
            throw new CustomException(ErrorCode.COMMON_INVALID_PARAMETER);
        }
        try {
            JsonNode response = restClientBuilder.build().get().uri(buildUri(category, page, perPage)).retrieve().body(JsonNode.class);
            List<SpotSyncPayload> items = data(response).stream()
                    .map(this::toPayload)
                    .filter(Objects::nonNull)
                    .filter(payload -> category == null || payload.getCategory() == category)
                    .toList();
            return SpotSyncPage.builder()
                    .items(items)
                    .page(page)
                    .size(perPage)
                    .totalCount(response == null ? 0 : response.path("totalCount").asInt(0))
                    .build();
        } catch (RestClientResponseException e) {
            log.warn("Media-location spot sync failed. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.SPOT_EXTERNAL_API_REQUEST_FAILED);
        } catch (RestClientException e) {
            log.warn("Media-location spot sync failed.", e);
            throw new CustomException(ErrorCode.SPOT_EXTERNAL_API_REQUEST_FAILED);
        }
    }

    private URI buildUri(SpotCategory category, int page, int perPage) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("page", page)
                .queryParam("perPage", perPage)
                .queryParam("returnType", "JSON");
        if (category != null) {
            builder.queryParam("cond[" + MEDIA_TYPE + "::EQ]", CATEGORY_TYPES.get(category).iterator().next());
        }
        String withoutKey = builder.encode(StandardCharsets.UTF_8).build(false).toUriString();
        return URI.create(withoutKey + "&serviceKey=" + encodedServiceKey());
    }

    private SpotSyncPayload toPayload(JsonNode item) {
        String mediaType = text(item, MEDIA_TYPE);
        SpotCategory category = toCategory(mediaType);
        String name = text(item, PLACE_NAME);
        String externalId = text(item, SEQUENCE);
        BigDecimal latitude = decimal(item, LATITUDE);
        BigDecimal longitude = decimal(item, LONGITUDE);
        if (category == null || !StringUtils.hasText(name) || !StringUtils.hasText(externalId) || latitude == null || longitude == null) {
            return null;
        }
        return SpotSyncPayload.builder()
                .source(ExternalSource.MEDIA_LOCATION_API)
                .externalId(externalId)
                .mediaType(mediaType)
                .contentTitle(text(item, TITLE))
                .nameKo(name)
                .placeType(text(item, PLACE_TYPE))
                .category(category)
                .addressKo(text(item, ADDRESS))
                .latitude(latitude)
                .longitude(longitude)
                .descriptionKo(description(item))
                .openingHours(text(item, OPENING_HOURS))
                .breakTime(text(item, BREAK_TIME))
                .closedDaysKo(text(item, CLOSED_DAYS))
                .tel(text(item, TEL))
                .build();
    }

    private List<JsonNode> data(JsonNode response) {
        JsonNode node = response == null ? null : response.path("data");
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<JsonNode> values = new ArrayList<>();
        node.forEach(values::add);
        return values;
    }

    private SpotCategory toCategory(String mediaType) {
        if (!StringUtils.hasText(mediaType)) {
            return null;
        }
        String normalized = mediaType.trim().toLowerCase(Locale.ROOT);
        return CATEGORY_TYPES.entrySet().stream().filter(entry -> entry.getValue().contains(normalized)).map(Map.Entry::getKey).findFirst().orElse(null);
    }

    private String description(JsonNode item) {
        String title = text(item, TITLE);
        String placeDescription = text(item, DESCRIPTION);
        if (!StringUtils.hasText(title)) {
            return placeDescription;
        }
        return StringUtils.hasText(placeDescription) ? "[" + title + "] " + placeDescription : title;
    }

    private String text(JsonNode node, String field) {
        if (node == null || node.path(field).isMissingNode() || node.path(field).isNull()) {
            return null;
        }
        String value = node.path(field).asText();
        return !StringUtils.hasText(value) || "정보없음".equals(value.trim()) ? null : value.trim();
    }

    private BigDecimal decimal(JsonNode node, String field) {
        String value = text(node, field);
        try {
            return StringUtils.hasText(value) ? new BigDecimal(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void validateServiceKey() {
        if (!StringUtils.hasText(serviceKey)) {
            throw new CustomException(ErrorCode.SPOT_EXTERNAL_API_KEY_MISSING);
        }
    }

    private String encodedServiceKey() {
        String trimmed = serviceKey.trim();
        return trimmed.contains("%") ? trimmed : UriUtils.encodeQueryParam(trimmed, StandardCharsets.UTF_8);
    }
}

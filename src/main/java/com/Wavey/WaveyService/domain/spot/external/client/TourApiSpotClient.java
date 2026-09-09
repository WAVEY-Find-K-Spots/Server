package com.Wavey.WaveyService.domain.spot.external.client;

import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.enums.SpotSourceType;
import com.Wavey.WaveyService.domain.spot.external.dto.ExternalSpotPage;
import com.Wavey.WaveyService.domain.spot.external.dto.ExternalSpotPayload;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;

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

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TourApiSpotClient {

    private static final String HERITAGE_CONTENT_TYPE_ID = "12";
    private static final String HERITAGE_CAT1 = "A02";
    private static final String HERITAGE_CAT2 = "A0201";
    private static final int THUMBNAIL_SEARCH_ROWS = 10;
    private static final int THUMBNAIL_MATCH_MIN_SCORE = 75;
    private static final double THUMBNAIL_MATCH_MAX_DISTANCE_KM = 2.0;
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final String NO_INFO = "\uC815\uBCF4\uC5C6\uC74C";

    private final RestClient.Builder restClientBuilder;

    @Value("${external-api.tour.base-url}")
    private String baseUrl;

    @Value("${external-api.service-key}")
    private String serviceKey;

    public List<ExternalSpotPayload> fetchHeritageSpots(int pageNo, int numOfRows) {
        return fetchHeritageSpotPage(pageNo, numOfRows).getItems();
    }

    public ExternalSpotPage fetchHeritageSpotPage(int pageNo, int numOfRows) {
        validateServiceKey();

        try {
            // DB-only mode: external HTTP request disabled.
            /* JsonNode response = restClientBuilder.build()
            .get()
            .uri(buildHeritageUri(pageNo, numOfRows))
            .retrieve()
            .body(JsonNode.class); */
            JsonNode response = externalRequestsDisabled();

            List<ExternalSpotPayload> items =
                    extractItems(response).stream()
                            .map(this::toHeritagePayload)
                            .filter(Objects::nonNull)
                            .toList();

            return ExternalSpotPage.builder()
                    .items(items)
                    .page(pageNo)
                    .size(numOfRows)
                    .totalCount(totalCount(response))
                    .build();
        } catch (RestClientResponseException e) {
            log.warn(
                    "관광 정보 API 요청 실패. 상태={}, 응답 본문={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.SPOT_EXTERNAL_API_REQUEST_FAILED);
        } catch (RestClientException e) {
            log.warn("관광 정보 API 요청에 실패했습니다.", e);
            throw new CustomException(ErrorCode.SPOT_EXTERNAL_API_REQUEST_FAILED);
        }
    }

    public Optional<String> findBestThumbnail(ExternalSpotPayload payload) {
        if (!isThumbnailSearchable(payload) || !StringUtils.hasText(serviceKey)) {
            return Optional.empty();
        }

        try {
            // DB-only mode: external HTTP request disabled.
            /* JsonNode response = restClientBuilder.build()
            .get()
            .uri(buildKeywordSearchUri(payload.getName()))
            .retrieve()
            .body(JsonNode.class); */
            JsonNode response = null; // No remote enrichment; retain stored thumbnails.

            return extractItems(response).stream()
                    .map(item -> toThumbnailCandidate(payload, item))
                    .flatMap(Optional::stream)
                    .filter(candidate -> candidate.score() >= THUMBNAIL_MATCH_MIN_SCORE)
                    .max(Comparator.comparingInt(ThumbnailCandidate::score))
                    .map(ThumbnailCandidate::thumbnailUrl);
        } catch (RestClientResponseException e) {
            log.warn(
                    "관광 정보 API 썸네일 보강 실패. 장소명={}, 상태={}, 응답 본문={}",
                    payload.getName(),
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            return Optional.empty();
        } catch (RestClientException e) {
            log.warn("관광 정보 API 썸네일 보강 실패. 장소명={}", payload.getName(), e);
            return Optional.empty();
        }
    }

    private URI buildHeritageUri(int pageNo, int numOfRows) {
        String uriWithoutServiceKey =
                UriComponentsBuilder.fromUriString(resolveEndpoint("/areaBasedList2"))
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "WAVEY")
                        .queryParam("_type", "json")
                        .queryParam("contentTypeId", HERITAGE_CONTENT_TYPE_ID)
                        .queryParam("cat1", HERITAGE_CAT1)
                        .queryParam("cat2", HERITAGE_CAT2)
                        .queryParam("arrange", "A")
                        .queryParam("pageNo", pageNo)
                        .queryParam("numOfRows", numOfRows)
                        .build(false)
                        .toUriString();

        return URI.create(uriWithoutServiceKey + "&serviceKey=" + encodedServiceKey());
    }

    private URI buildKeywordSearchUri(String keyword) {
        String uriWithoutServiceKey =
                UriComponentsBuilder.fromUriString(resolveEndpoint("/searchKeyword2"))
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "WAVEY")
                        .queryParam("_type", "json")
                        .queryParam("listYN", "Y")
                        .queryParam("arrange", "O")
                        .queryParam("pageNo", 1)
                        .queryParam("numOfRows", THUMBNAIL_SEARCH_ROWS)
                        .queryParam("keyword", keyword)
                        .encode(StandardCharsets.UTF_8)
                        .build(false)
                        .toUriString();

        return URI.create(uriWithoutServiceKey + "&serviceKey=" + encodedServiceKey());
    }

    private String resolveEndpoint(String path) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + path;
        }
        return baseUrl + path;
    }

    private List<JsonNode> extractItems(JsonNode response) {
        JsonNode itemNode =
                response == null
                        ? null
                        : response.path("response").path("body").path("items").path("item");

        if (itemNode == null || itemNode.isMissingNode() || itemNode.isNull()) {
            return List.of();
        }

        if (itemNode.isArray()) {
            List<JsonNode> items = new ArrayList<>();
            itemNode.forEach(items::add);
            return items;
        }

        return List.of(itemNode);
    }

    private int totalCount(JsonNode response) {
        if (response == null) {
            return 0;
        }
        return response.path("response").path("body").path("totalCount").asInt(0);
    }

    private ExternalSpotPayload toHeritagePayload(JsonNode item) {
        String name = text(item, "title");
        String contentId = text(item, "contentid");
        BigDecimal latitude = decimal(item, "mapy");
        BigDecimal longitude = decimal(item, "mapx");

        if (!StringUtils.hasText(name)
                || !StringUtils.hasText(contentId)
                || latitude == null
                || longitude == null) {
            return null;
        }

        return ExternalSpotPayload.builder()
                .name(name)
                .category(SpotCategory.K_HERITAGE)
                .address(address(item))
                .latitude(latitude)
                .longitude(longitude)
                .description(text(item, "overview"))
                .tel(text(item, "tel"))
                .thumbnailUrl(text(item, "firstimage"))
                .sourceType(SpotSourceType.TOUR_API)
                .externalContentId(contentId)
                .build();
    }

    private Optional<ThumbnailCandidate> toThumbnailCandidate(
            ExternalSpotPayload payload, JsonNode item) {
        String thumbnailUrl = thumbnailUrl(item);
        if (!StringUtils.hasText(thumbnailUrl)) {
            return Optional.empty();
        }

        int nameScore = nameScore(payload.getName(), text(item, "title"));
        if (nameScore == 0) {
            return Optional.empty();
        }

        int distanceScore =
                distanceScore(
                        payload.getLatitude(),
                        payload.getLongitude(),
                        decimal(item, "mapy"),
                        decimal(item, "mapx"));
        if (distanceScore < 0) {
            return Optional.empty();
        }

        int score = nameScore + addressScore(payload.getAddress(), address(item)) + distanceScore;
        return Optional.of(new ThumbnailCandidate(thumbnailUrl, score));
    }

    private String thumbnailUrl(JsonNode item) {
        String firstImage = text(item, "firstimage");
        if (StringUtils.hasText(firstImage)) {
            return firstImage;
        }
        return text(item, "firstimage2");
    }

    private int nameScore(String sourceName, String candidateName) {
        String normalizedSourceName = normalizeName(sourceName);
        String normalizedCandidateName = normalizeName(candidateName);

        if (!StringUtils.hasText(normalizedSourceName)
                || !StringUtils.hasText(normalizedCandidateName)) {
            return 0;
        }
        if (normalizedSourceName.equals(normalizedCandidateName)) {
            return 60;
        }
        if (normalizedSourceName.length() >= 3
                && normalizedCandidateName.length() >= 3
                && (normalizedSourceName.contains(normalizedCandidateName)
                        || normalizedCandidateName.contains(normalizedSourceName))) {
            return 35;
        }
        return 0;
    }

    private int addressScore(String sourceAddress, String candidateAddress) {
        List<String> sourceTokens = addressTokens(sourceAddress);
        List<String> candidateTokens = addressTokens(candidateAddress);

        if (sourceTokens.isEmpty() || candidateTokens.isEmpty()) {
            return 0;
        }

        long overlapCount = sourceTokens.stream().filter(candidateTokens::contains).count();

        if (overlapCount >= 3) {
            return 25;
        }
        if (overlapCount == 2) {
            return 18;
        }
        if (overlapCount == 1) {
            return 8;
        }
        return 0;
    }

    private List<String> addressTokens(String address) {
        String normalizedAddress = normalizeAddress(address);
        if (!StringUtils.hasText(normalizedAddress)) {
            return List.of();
        }

        return Arrays.stream(normalizedAddress.split(" "))
                .filter(token -> token.length() >= 2)
                .distinct()
                .toList();
    }

    private int distanceScore(
            BigDecimal sourceLatitude,
            BigDecimal sourceLongitude,
            BigDecimal candidateLatitude,
            BigDecimal candidateLongitude) {
        if (sourceLatitude == null
                || sourceLongitude == null
                || candidateLatitude == null
                || candidateLongitude == null) {
            return 0;
        }

        double distanceKm =
                distanceKm(
                        sourceLatitude.doubleValue(),
                        sourceLongitude.doubleValue(),
                        candidateLatitude.doubleValue(),
                        candidateLongitude.doubleValue());

        if (distanceKm > THUMBNAIL_MATCH_MAX_DISTANCE_KM) {
            return -1;
        }
        if (distanceKm <= 0.1) {
            return 30;
        }
        if (distanceKm <= 0.5) {
            return 25;
        }
        if (distanceKm <= 1.0) {
            return 15;
        }
        return 5;
    }

    private double distanceKm(
            double sourceLatitude,
            double sourceLongitude,
            double candidateLatitude,
            double candidateLongitude) {
        double latDistance = Math.toRadians(candidateLatitude - sourceLatitude);
        double lngDistance = Math.toRadians(candidateLongitude - sourceLongitude);
        double sourceLatRadians = Math.toRadians(sourceLatitude);
        double candidateLatRadians = Math.toRadians(candidateLatitude);

        double haversine =
                Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                        + Math.cos(sourceLatRadians)
                                * Math.cos(candidateLatRadians)
                                * Math.sin(lngDistance / 2)
                                * Math.sin(lngDistance / 2);

        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }

    private String normalizeName(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("\\([^)]*\\)", "")
                .replaceAll("[\\s\\p{Punct}·ㆍ-]", "")
                .trim();
    }

    private String normalizeAddress(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[\\p{Punct}·ㆍ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean isThumbnailSearchable(ExternalSpotPayload payload) {
        return payload != null
                && StringUtils.hasText(payload.getName())
                && payload.getLatitude() != null
                && payload.getLongitude() != null;
    }

    private String address(JsonNode item) {
        String addr1 = text(item, "addr1");
        String addr2 = text(item, "addr2");

        if (!StringUtils.hasText(addr1)) {
            return addr2;
        }
        if (!StringUtils.hasText(addr2)) {
            return addr1;
        }
        return addr1 + " " + addr2;
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

    private String encodedServiceKey() {
        String trimmedServiceKey = serviceKey.trim();
        if (trimmedServiceKey.contains("%")) {
            return trimmedServiceKey;
        }
        return UriUtils.encodeQueryParam(trimmedServiceKey, StandardCharsets.UTF_8);
    }

    private record ThumbnailCandidate(String thumbnailUrl, int score) {}

    private JsonNode externalRequestsDisabled() {
        throw new CustomException(ErrorCode.EXTERNAL_API_DISABLED);
    }
}

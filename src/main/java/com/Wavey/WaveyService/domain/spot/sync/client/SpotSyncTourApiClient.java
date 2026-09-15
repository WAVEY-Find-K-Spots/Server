package com.Wavey.WaveyService.domain.spot.sync.client;

import com.Wavey.WaveyService.domain.spot.enums.ExternalSource;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.sync.dto.SpotSyncPage;
import com.Wavey.WaveyService.domain.spot.sync.dto.SpotSyncPayload;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
public class SpotSyncTourApiClient {

    private static final double EARTH_RADIUS_METERS = 6371000.0;
    private static final double ENGLISH_MATCH_MAX_DISTANCE_METERS = 500.0;

    private final RestClient.Builder restClientBuilder;

    @Value("${external-api.tour.base-url}")
    private String baseUrl;

    @Value("${external-api.tour.eng-base-url:https://apis.data.go.kr/B551011/EngService2}")
    private String englishBaseUrl;

    @Value("${external-api.service-key:}")
    private String serviceKey;

    public SpotSyncPage fetchHeritage(int pageNo, int numOfRows) {
        validateServiceKey();
        try {
            JsonNode response = restClientBuilder.build().get().uri(buildUri(pageNo, numOfRows)).retrieve().body(JsonNode.class);
            List<SpotSyncPayload> items = extractItems(response).stream().map(this::toPayload).filter(Objects::nonNull).toList();
            return SpotSyncPage.builder().items(items).page(pageNo).size(numOfRows).totalCount(totalCount(response)).build();
        } catch (RestClientResponseException e) {
            log.warn("TourAPI spot sync failed. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.SPOT_EXTERNAL_API_REQUEST_FAILED);
        } catch (RestClientException e) {
            log.warn("TourAPI spot sync failed.", e);
            throw new CustomException(ErrorCode.SPOT_EXTERNAL_API_REQUEST_FAILED);
        }
    }

    public SpotSyncPage searchPlace(String keyword, int numOfRows) {
        validateServiceKey();
        try {
            JsonNode response = restClientBuilder.build().get().uri(buildSearchUri(keyword, numOfRows)).retrieve().body(JsonNode.class);
            List<SpotSyncPayload> items = extractItems(response).stream().map(this::toPayload).filter(Objects::nonNull).toList();
            return SpotSyncPage.builder().items(items).page(1).size(numOfRows).totalCount(totalCount(response)).build();
        } catch (RestClientResponseException e) {
            log.warn("TourAPI place search failed. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.SPOT_EXTERNAL_API_REQUEST_FAILED);
        } catch (RestClientException e) {
            log.warn("TourAPI place search failed.", e);
            throw new CustomException(ErrorCode.SPOT_EXTERNAL_API_REQUEST_FAILED);
        }
    }

    public SpotSyncPage enrichPlace(String keyword) {
        SpotSyncPage searchPage = searchPlace(keyword, 3);
        List<SpotSyncPayload> candidates = searchPage.getItems() == null ? List.of() : searchPage.getItems();
        List<SpotSyncPayload> exactMatches = candidates.stream()
                .filter(candidate -> isExactTitleMatch(keyword, candidate.getNameKo()))
                .toList();
        if (exactMatches.size() == 1) {
            candidates = exactMatches;
        }
        if (candidates.size() != 1) {
            return SpotSyncPage.builder()
                    .items(List.of())
                    .page(1)
                    .size(0)
                    .totalCount(searchPage.getTotalCount())
                    .build();
        }
        SpotSyncPayload enriched = enrichDetails(candidates.get(0));
        return SpotSyncPage.builder()
                .items(List.of(enriched))
                .page(1)
                .size(1)
                .totalCount(1)
                .build();
    }

    private SpotSyncPayload enrichDetails(SpotSyncPayload base) {
        JsonNode common = fetchFirstItem(detailCommonUri(base.getExternalId()));
        JsonNode intro = fetchFirstItem(detailIntroUri(base.getExternalId(), base.getExternalContentTypeId()));
        JsonNode image = fetchFirstItem(detailImageUri(base.getExternalId()));
        SpotSyncPayload koreanPayload = base.toBuilder()
                .nameKo(coalesce(text(common, "title"), base.getNameKo()))
                .placeType(coalesce(toPlaceType(base.getExternalContentTypeId()), base.getPlaceType()))
                .addressKo(coalesce(address(common), base.getAddressKo()))
                .latitude(coalesce(decimal(common, "mapy"), base.getLatitude()))
                .longitude(coalesce(decimal(common, "mapx"), base.getLongitude()))
                .descriptionKo(coalesce(text(common, "overview"), base.getDescriptionKo()))
                .tel(coalesce(introText(intro, "infocenter"), text(common, "tel"), base.getTel()))
                .openingHours(coalesce(introText(intro, "usetime"), base.getOpeningHours()))
                .breakTime(coalesce(introText(intro, "breaktime"), base.getBreakTime()))
                .closedDaysKo(coalesce(introText(intro, "restdate"), base.getClosedDaysKo()))
                .imageUrl(coalesce(text(image, "originimgurl"), text(common, "firstimage"), base.getImageUrl()))
                .build();

        JsonNode englishCommon = findHighConfidenceEnglishCommon(koreanPayload);
        if (englishCommon == null) {
            return koreanPayload;
        }

        return koreanPayload.toBuilder()
                .nameEn(coalesce(text(englishCommon, "title"), koreanPayload.getNameEn()))
                .addressEn(coalesce(address(englishCommon), koreanPayload.getAddressEn()))
                .descriptionEn(coalesce(text(englishCommon, "overview"), koreanPayload.getDescriptionEn()))
                .build();
    }

    private boolean isExactTitleMatch(String keyword, String title) {
        if (!StringUtils.hasText(keyword) || !StringUtils.hasText(title)) {
            return false;
        }
        return normalizeTitle(keyword).equals(normalizeTitle(title));
    }

    private String normalizeTitle(String value) {
        return value.replaceAll("\\([^)]*\\)", "")
                .replaceAll("\\[[^]]*]", "")
                .replaceAll("\\s+", "")
                .trim();
    }

    private JsonNode findHighConfidenceEnglishCommon(SpotSyncPayload koreanPayload) {
        List<JsonNode> candidates = fetchItems(englishSearchUri(koreanPayload.getNameKo(), 5));
        return candidates.stream()
                .filter(candidate -> isHighConfidenceEnglishMatch(koreanPayload, candidate))
                .min(Comparator.comparingDouble(candidate -> distanceMeters(
                        koreanPayload.getLatitude(),
                        koreanPayload.getLongitude(),
                        decimal(candidate, "mapy"),
                        decimal(candidate, "mapx"))))
                .map(candidate -> fetchFirstItem(englishDetailCommonUri(text(candidate, "contentid"))))
                .orElse(null);
    }

    private boolean isHighConfidenceEnglishMatch(SpotSyncPayload koreanPayload, JsonNode candidate) {
        BigDecimal sourceLat = koreanPayload.getLatitude();
        BigDecimal sourceLng = koreanPayload.getLongitude();
        BigDecimal candidateLat = decimal(candidate, "mapy");
        BigDecimal candidateLng = decimal(candidate, "mapx");
        if (sourceLat == null || sourceLng == null || candidateLat == null || candidateLng == null) {
            return false;
        }
        return distanceMeters(sourceLat, sourceLng, candidateLat, candidateLng) <= ENGLISH_MATCH_MAX_DISTANCE_METERS;
    }

    private double distanceMeters(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLng = Math.toRadians(lng2.doubleValue() - lng1.doubleValue());
        double rLat1 = Math.toRadians(lat1.doubleValue());
        double rLat2 = Math.toRadians(lat2.doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(rLat1) * Math.cos(rLat2) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private List<JsonNode> fetchItems(URI uri) {
        try {
            JsonNode response = restClientBuilder.build().get().uri(uri).retrieve().body(JsonNode.class);
            return extractItems(response);
        } catch (RestClientException e) {
            log.warn("TourAPI candidate fetch failed.", e);
            return List.of();
        }
    }

    private URI englishSearchUri(String keyword, int numOfRows) {
        String withoutKey = UriComponentsBuilder.fromUriString(englishEndpoint("/searchKeyword2"))
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "WAVEY")
                .queryParam("_type", "json")
                .queryParam("arrange", "A")
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", numOfRows)
                .queryParam("keyword", keyword)
                .encode(StandardCharsets.UTF_8)
                .build(false)
                .toUriString();
        return withServiceKey(withoutKey);
    }

    private URI englishDetailCommonUri(String contentId) {
        return withServiceKey(UriComponentsBuilder.fromUriString(englishEndpoint("/detailCommon2"))
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "WAVEY")
                .queryParam("_type", "json")
                .queryParam("contentId", contentId)
                .build(false)
                .toUriString());
    }

    private String englishEndpoint(String path) {
        return englishBaseUrl.endsWith("/") ? englishBaseUrl.substring(0, englishBaseUrl.length() - 1) + path : englishBaseUrl + path;
    }

    private URI buildUri(int pageNo, int numOfRows) {
        String withoutKey = UriComponentsBuilder.fromUriString(endpoint("/areaBasedList2"))
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "WAVEY")
                .queryParam("_type", "json")
                .queryParam("contentTypeId", "12")
                .queryParam("cat1", "A02")
                .queryParam("cat2", "A0201")
                .queryParam("arrange", "A")
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", numOfRows)
                .build(false)
                .toUriString();
        return URI.create(withoutKey + "&serviceKey=" + encodedServiceKey());
    }

    private URI buildSearchUri(String keyword, int numOfRows) {
        String withoutKey = UriComponentsBuilder.fromUriString(endpoint("/searchKeyword2"))
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "WAVEY")
                .queryParam("_type", "json")
                .queryParam("arrange", "A")
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", numOfRows)
                .queryParam("keyword", keyword)
                .encode(StandardCharsets.UTF_8)
                .build(false)
                .toUriString();
        return URI.create(withoutKey + "&serviceKey=" + encodedServiceKey());
    }

    private URI detailCommonUri(String contentId) {
        return withServiceKey(UriComponentsBuilder.fromUriString(endpoint("/detailCommon2"))
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "WAVEY")
                .queryParam("_type", "json")
                .queryParam("contentId", contentId)
                .build(false)
                .toUriString());
    }

    private URI detailIntroUri(String contentId, String contentTypeId) {
        return withServiceKey(UriComponentsBuilder.fromUriString(endpoint("/detailIntro2"))
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "WAVEY")
                .queryParam("_type", "json")
                .queryParam("contentId", contentId)
                .queryParam("contentTypeId", contentTypeId)
                .build(false)
                .toUriString());
    }

    private URI detailImageUri(String contentId) {
        return withServiceKey(UriComponentsBuilder.fromUriString(endpoint("/detailImage2"))
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "WAVEY")
                .queryParam("_type", "json")
                .queryParam("contentId", contentId)
                .queryParam("imageYN", "Y")
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 1)
                .build(false)
                .toUriString());
    }

    private URI withServiceKey(String uriWithoutServiceKey) {
        return URI.create(uriWithoutServiceKey + "&serviceKey=" + encodedServiceKey());
    }

    private String endpoint(String path) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) + path : baseUrl + path;
    }

    private JsonNode fetchFirstItem(URI uri) {
        try {
            JsonNode response = restClientBuilder.build().get().uri(uri).retrieve().body(JsonNode.class);
            return extractItems(response).stream().findFirst().orElse(null);
        } catch (RestClientException e) {
            log.warn("TourAPI detail enrichment failed.", e);
            return null;
        }
    }

    private SpotSyncPayload toPayload(JsonNode item) {
        String name = text(item, "title");
        String contentId = text(item, "contentid");
        BigDecimal latitude = decimal(item, "mapy");
        BigDecimal longitude = decimal(item, "mapx");
        if (!StringUtils.hasText(name) || !StringUtils.hasText(contentId) || latitude == null || longitude == null) {
            return null;
        }
        return SpotSyncPayload.builder()
                .source(ExternalSource.TOUR_API)
                .externalId(contentId)
                .externalContentTypeId(text(item, "contenttypeid"))
                .nameKo(name)
                .category(SpotCategory.K_HERITAGE)
                .placeType(toPlaceType(text(item, "contenttypeid")))
                .addressKo(address(item))
                .latitude(latitude)
                .longitude(longitude)
                .descriptionKo(text(item, "overview"))
                .tel(text(item, "tel"))
                .imageUrl(text(item, "firstimage"))
                .build();
    }

    private List<JsonNode> extractItems(JsonNode response) {
        JsonNode node = response == null ? null : response.path("response").path("body").path("items").path("item");
        if (node == null || node.isMissingNode() || node.isNull()) {
            return List.of();
        }
        if (!node.isArray()) {
            return List.of(node);
        }
        List<JsonNode> items = new ArrayList<>();
        node.forEach(items::add);
        return items;
    }

    private int totalCount(JsonNode response) {
        return response == null ? 0 : response.path("response").path("body").path("totalCount").asInt(0);
    }

    private String address(JsonNode item) {
        String addr1 = text(item, "addr1");
        String addr2 = text(item, "addr2");
        if (!StringUtils.hasText(addr1)) {
            return addr2;
        }
        return StringUtils.hasText(addr2) ? addr1 + " " + addr2 : addr1;
    }

    private String text(JsonNode node, String field) {
        if (node == null || node.path(field).isMissingNode() || node.path(field).isNull()) {
            return null;
        }
        String value = node.path(field).asText();
        return !StringUtils.hasText(value) || "정보없음".equals(value.trim()) ? null : value.trim();
    }

    private String introText(JsonNode node, String normalizedField) {
        return switch (normalizedField) {
            case "infocenter" -> coalesce(
                    text(node, "infocenter"),
                    text(node, "infocenterfood"),
                    text(node, "infocenterlodging"),
                    text(node, "infocentershopping"));
            case "usetime" -> coalesce(
                    text(node, "usetime"),
                    text(node, "opentimefood"),
                    text(node, "checkintime"),
                    text(node, "opentime"));
            case "breaktime" -> coalesce(
                    text(node, "breaktime"),
                    text(node, "breaktimefood"));
            case "restdate" -> coalesce(
                    text(node, "restdate"),
                    text(node, "restdatefood"),
                    text(node, "restdateleports"),
                    text(node, "restdateshopping"));
            default -> null;
        };
    }

    private String toPlaceType(String contentTypeId) {
        if (!StringUtils.hasText(contentTypeId)) {
            return "OTHER";
        }
        return switch (contentTypeId.trim()) {
            case "32" -> "STAY";
            case "39" -> "RESTAURANT";
            case "38" -> "SHOP";
            case "25" -> "PLAYGROUND";
            default -> "OTHER";
        };
    }

    private BigDecimal decimal(JsonNode node, String field) {
        String value = text(node, field);
        try {
            return StringUtils.hasText(value) ? new BigDecimal(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String coalesce(String first, String... rest) {
        if (StringUtils.hasText(first)) {
            return first;
        }
        for (String value : rest) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private BigDecimal coalesce(BigDecimal first, BigDecimal second) {
        return first != null ? first : second;
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

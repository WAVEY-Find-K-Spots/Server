package com.Wavey.WaveyService.domain.route.directions.client;

import com.Wavey.WaveyService.domain.route.entity.TransportMode;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Tmap 오픈API 길찾기 클라이언트.
 * <ul>
 *     <li>{@code WALK} → 보행자 경로 안내 ({@code /tmap/routes/pedestrian})</li>
 *     <li>{@code CAR} → 자동차 경로 안내 ({@code /tmap/routes})</li>
 *     <li>{@code TRANSIT} → 대중교통 경로 안내 ({@code /transit/routes})</li>
 * </ul>
 * 좌표는 모두 WGS84 경위도({@code x=경도}, {@code y=위도})를 사용한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TmapDirectionsClient {

    private static final String COORD_TYPE = "WGS84GEO";

    private final RestClient.Builder restClientBuilder;

    @Value("${tmap.base-url}")
    private String baseUrl;

    @Value("${tmap.app-key:}")
    private String appKey;

    public RouteLeg route(TransportMode mode, double fromLng, double fromLat, double toLng, double toLat) {
        if (!StringUtils.hasText(appKey)) {
            log.warn("Tmap app key is not configured.");
            throw new CustomException(ErrorCode.DIRECTIONS_PROVIDER_ERROR);
        }

        try {
            return switch (mode) {
                case WALK -> requestRoadRoute("/tmap/routes/pedestrian", fromLng, fromLat, toLng, toLat);
                case CAR -> requestRoadRoute("/tmap/routes", fromLng, fromLat, toLng, toLat);
                case TRANSIT -> requestTransitRoute(fromLng, fromLat, toLng, toLat);
            };
        } catch (RestClientResponseException e) {
            log.warn("Tmap directions request failed. mode={}, status={}, body={}",
                    mode, e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.DIRECTIONS_PROVIDER_ERROR);
        } catch (RestClientException e) {
            log.warn("Tmap directions request failed. mode={}", mode, e);
            throw new CustomException(ErrorCode.DIRECTIONS_PROVIDER_ERROR);
        }
    }

    private RouteLeg requestRoadRoute(String path, double fromLng, double fromLat, double toLng, double toLat) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("version", "1");
        form.add("format", "json");
        form.add("startX", String.valueOf(fromLng));
        form.add("startY", String.valueOf(fromLat));
        form.add("endX", String.valueOf(toLng));
        form.add("endY", String.valueOf(toLat));
        form.add("startName", "start");
        form.add("endName", "end");
        form.add("reqCoordType", COORD_TYPE);
        form.add("resCoordType", COORD_TYPE);

        JsonNode response = restClientBuilder.build()
                .post()
                .uri(resolveEndpoint(path))
                .header("appKey", appKey)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(JsonNode.class);

        return parseRoadRoute(response);
    }

    private RouteLeg parseRoadRoute(JsonNode response) {
        JsonNode features = response == null ? null : response.path("features");
        if (features == null || !features.isArray() || features.isEmpty()) {
            throw new CustomException(ErrorCode.DIRECTIONS_PROVIDER_ERROR);
        }

        long distanceMeters = 0L;
        long durationSeconds = 0L;
        List<double[]> pathCoords = new ArrayList<>();

        for (JsonNode feature : features) {
            JsonNode properties = feature.path("properties");
            if (properties.has("totalDistance")) {
                distanceMeters = properties.path("totalDistance").asLong(distanceMeters);
            }
            if (properties.has("totalTime")) {
                durationSeconds = properties.path("totalTime").asLong(durationSeconds);
            }

            JsonNode geometry = feature.path("geometry");
            if (!"LineString".equals(geometry.path("type").asText())) {
                continue;
            }
            for (JsonNode coord : geometry.path("coordinates")) {
                if (coord.isArray() && coord.size() >= 2) {
                    appendCoord(pathCoords, coord.get(0).asDouble(), coord.get(1).asDouble());
                }
            }
        }

        return new RouteLeg(distanceMeters, durationSeconds, pathCoords);
    }

    private RouteLeg requestTransitRoute(double fromLng, double fromLat, double toLng, double toLat) {
        String body = """
                {"startX":"%s","startY":"%s","endX":"%s","endY":"%s","count":1,"format":"json"}
                """.formatted(fromLng, fromLat, toLng, toLat);

        JsonNode response = restClientBuilder.build()
                .post()
                .uri(resolveEndpoint("/transit/routes"))
                .header("appKey", appKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        return parseTransitRoute(response);
    }

    private RouteLeg parseTransitRoute(JsonNode response) {
        JsonNode itineraries = response == null
                ? null
                : response.path("metaData").path("plan").path("itineraries");
        if (itineraries == null || !itineraries.isArray() || itineraries.isEmpty()) {
            throw new CustomException(ErrorCode.DIRECTIONS_PROVIDER_ERROR);
        }

        JsonNode best = itineraries.get(0);
        long distanceMeters = best.path("totalDistance").asLong(0L);
        long durationSeconds = best.path("totalTime").asLong(0L);

        List<double[]> pathCoords = new ArrayList<>();
        for (JsonNode leg : best.path("legs")) {
            String linestring = leg.path("passShape").path("linestring").asText("");
            if (StringUtils.hasText(linestring)) {
                appendLinestring(pathCoords, linestring);
                continue;
            }
            appendPoint(pathCoords, leg.path("start"));
            appendPoint(pathCoords, leg.path("end"));
        }

        return new RouteLeg(distanceMeters, durationSeconds, pathCoords);
    }

    private void appendLinestring(List<double[]> pathCoords, String linestring) {
        for (String pair : linestring.trim().split("\\s+")) {
            String[] lngLat = pair.split(",");
            if (lngLat.length >= 2) {
                appendCoord(pathCoords, parseDouble(lngLat[0]), parseDouble(lngLat[1]));
            }
        }
    }

    private void appendPoint(List<double[]> pathCoords, JsonNode point) {
        if (point.has("lon") && point.has("lat")) {
            appendCoord(pathCoords, point.path("lon").asDouble(), point.path("lat").asDouble());
        }
    }

    private void appendCoord(List<double[]> pathCoords, double lng, double lat) {
        if (!pathCoords.isEmpty()) {
            double[] last = pathCoords.get(pathCoords.size() - 1);
            if (last[0] == lng && last[1] == lat) {
                return;
            }
        }
        pathCoords.add(new double[] {lng, lat});
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String resolveEndpoint(String path) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + path;
        }
        return baseUrl + path;
    }
}

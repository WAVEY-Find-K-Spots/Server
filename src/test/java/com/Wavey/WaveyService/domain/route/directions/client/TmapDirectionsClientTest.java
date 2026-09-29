package com.Wavey.WaveyService.domain.route.directions.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class TmapDirectionsClientTest {

    private final TmapDirectionsClient client = new TmapDirectionsClient(RestClient.builder());
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BUS_ITINERARY = """
            {
              "totalTime": 1080, "totalDistance": 4300, "totalWalkTime": 360, "totalWalkDistance": 420,
              "transferCount": 0, "fare": {"regular": {"totalFare": 1500}},
              "legs": [
                {"mode": "WALK", "sectionTime": 120, "distance": 150,
                 "start": {"name": "출발지", "lon": 127.0, "lat": 37.5},
                 "end": {"name": "경복궁", "lon": 127.001, "lat": 37.501},
                 "steps": [{"linestring": "127.0,37.5 127.001,37.501"}]},
                {"mode": "BUS", "route": "간선:273", "routeColor": "0068B7", "sectionTime": 720, "distance": 3900,
                 "start": {"name": "경복궁", "lon": 127.001, "lat": 37.501},
                 "end": {"name": "안국역", "lon": 127.01, "lat": 37.51},
                 "passStopList": {"stationList": [
                   {"stationName": "경복궁"}, {"stationName": "국립민속박물관"}, {"stationName": "안국역"}]},
                 "passShape": {"linestring": "127.001,37.501 127.005,37.505 127.01,37.51"}}
              ]
            }
            """;

    private static final String SUBWAY_ITINERARY = """
            {
              "totalTime": 1320, "totalDistance": 5100, "totalWalkTime": 540, "totalWalkDistance": 700,
              "transferCount": 1,
              "legs": [
                {"mode": "SUBWAY", "route": "수도권3호선", "routeColor": "EF7C1C", "sectionTime": 600, "distance": 3000,
                 "start": {"name": "경복궁", "lon": 127.0, "lat": 37.5},
                 "end": {"name": "안국", "lon": 127.02, "lat": 37.52}}
              ]
            }
            """;

    private JsonNode transitResponse(String... itineraries) throws Exception {
        return objectMapper.readTree("""
                {"metaData": {"plan": {"itineraries": [%s]}}}
                """.formatted(String.join(",", itineraries)));
    }

    @Test
    void 대중교통_경로_후보를_모두_파싱하고_첫_후보를_대표로_사용한다() throws Exception {
        RouteLeg leg = client.parseTransitRoute(transitResponse(BUS_ITINERARY, SUBWAY_ITINERARY));

        assertThat(leg.transitOptions()).hasSize(2);
        assertThat(leg.durationSeconds()).isEqualTo(1080);
        assertThat(leg.distanceMeters()).isEqualTo(4300);
        assertThat(leg.transitLegs()).isEqualTo(leg.transitOptions().get(0).legs());

        TransitItinerary bus = leg.transitOptions().get(0);
        assertThat(bus.walkSeconds()).isEqualTo(360);
        assertThat(bus.walkDistanceMeters()).isEqualTo(420);
        assertThat(bus.transferCount()).isZero();
        assertThat(bus.fare()).isEqualTo(1500);
        assertThat(bus.path()).hasSize(4);

        TransitLegDetail busLeg = bus.legs().get(1);
        assertThat(busLeg.routeName()).isEqualTo("간선:273");
        assertThat(busLeg.stationCount()).isEqualTo(2);
        assertThat(busLeg.passStops()).containsExactly("경복궁", "국립민속박물관", "안국역");

        TransitItinerary subway = leg.transitOptions().get(1);
        assertThat(subway.transferCount()).isEqualTo(1);
        assertThat(subway.fare()).isZero();
        assertThat(subway.legs().get(0).stationCount()).isZero();
        assertThat(subway.legs().get(0).passStops()).isEmpty();
    }

    @Test
    void 대중교통_경로_후보는_최대_5개까지만_사용한다() throws Exception {
        RouteLeg leg = client.parseTransitRoute(transitResponse(
                BUS_ITINERARY, SUBWAY_ITINERARY, BUS_ITINERARY, SUBWAY_ITINERARY,
                BUS_ITINERARY, SUBWAY_ITINERARY));

        assertThat(leg.transitOptions()).hasSize(5);
    }

    @Test
    void 대중교통_경로가_없으면_예외가_발생한다() throws Exception {
        assertThatThrownBy(() -> client.parseTransitRoute(transitResponse()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DIRECTIONS_PROVIDER_ERROR);
    }
}

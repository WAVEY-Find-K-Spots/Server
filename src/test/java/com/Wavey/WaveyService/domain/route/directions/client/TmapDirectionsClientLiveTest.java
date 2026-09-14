package com.Wavey.WaveyService.domain.route.directions.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.Wavey.WaveyService.domain.route.entity.TransportMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

/**
 * 실제 Tmap 오픈API 를 호출하는 라이브 테스트.
 * {@code TMAP_APP_KEY} 환경변수가 설정된 경우에만 실행된다. (CI 기본 실행에서는 skip)
 *
 * <pre>{@code TMAP_APP_KEY=<발급키> ./gradlew test --tests '*TmapDirectionsClientLiveTest'}</pre>
 */
@EnabledIfEnvironmentVariable(named = "TMAP_APP_KEY", matches = ".+")
class TmapDirectionsClientLiveTest {

    // 경복궁 → 북촌한옥마을
    private static final double FROM_LNG = 126.977041;
    private static final double FROM_LAT = 37.579617;
    private static final double TO_LNG = 126.983746;
    private static final double TO_LAT = 37.582604;

    private TmapDirectionsClient client() {
        TmapDirectionsClient client = new TmapDirectionsClient(RestClient.builder());
        ReflectionTestUtils.setField(client, "baseUrl", "https://apis.openapi.sk.com");
        ReflectionTestUtils.setField(client, "appKey", System.getenv("TMAP_APP_KEY"));
        return client;
    }

    @Test
    void 보행자_경로를_실제로_불러온다() {
        RouteLeg leg = client().route(TransportMode.WALK, FROM_LNG, FROM_LAT, TO_LNG, TO_LAT);

        System.out.printf("[WALK] distance=%dm, duration=%ds, points=%d%n",
                leg.distanceMeters(), leg.durationSeconds(), leg.path().size());

        assertThat(leg.distanceMeters()).isPositive();
        assertThat(leg.durationSeconds()).isPositive();
        assertThat(leg.path()).hasSizeGreaterThan(1);
        assertThat(leg.path().get(0)).hasSize(2);
    }

    @Test
    void 자동차_경로를_실제로_불러온다() {
        RouteLeg leg = client().route(TransportMode.CAR, FROM_LNG, FROM_LAT, TO_LNG, TO_LAT);

        System.out.printf("[CAR] distance=%dm, duration=%ds, points=%d%n",
                leg.distanceMeters(), leg.durationSeconds(), leg.path().size());

        assertThat(leg.distanceMeters()).isPositive();
        assertThat(leg.durationSeconds()).isPositive();
        assertThat(leg.path()).hasSizeGreaterThan(1);
    }

    @Test
    void 대중교통_경로를_실제로_불러온다() {
        RouteLeg leg = client().route(TransportMode.TRANSIT, FROM_LNG, FROM_LAT, TO_LNG, TO_LAT);

        System.out.printf("[TRANSIT] distance=%dm, duration=%ds, points=%d%n",
                leg.distanceMeters(), leg.durationSeconds(), leg.path().size());

        assertThat(leg.durationSeconds()).isPositive();
        assertThat(leg.path()).hasSizeGreaterThan(1);
    }
}

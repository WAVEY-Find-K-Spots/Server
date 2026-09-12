package com.Wavey.WaveyService.domain.route.directions.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.Wavey.WaveyService.domain.route.directions.client.RouteLeg;
import com.Wavey.WaveyService.domain.route.directions.client.TmapDirectionsClient;
import com.Wavey.WaveyService.domain.route.dto.request.RouteDirectionsRequest;
import com.Wavey.WaveyService.domain.route.dto.response.RouteDirectionsResponse;
import com.Wavey.WaveyService.domain.route.entity.Route;
import com.Wavey.WaveyService.domain.route.entity.RouteSpot;
import com.Wavey.WaveyService.domain.route.entity.TransportMode;
import com.Wavey.WaveyService.domain.route.entity.Visibility;
import com.Wavey.WaveyService.domain.route.repository.RouteRepository;
import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RouteDirectionsServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private SpotRepository spotRepository;

    @Mock
    private TmapDirectionsClient tmapDirectionsClient;

    @InjectMocks
    private RouteDirectionsService routeDirectionsService;

    private final Long routeId = 1L;
    private final Long ownerId = 100L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(routeDirectionsService, "cacheTtlSeconds", 300L);
    }

    private Route routeWithSpots(Visibility visibility, RouteSpot... spots) {
        Route route = Route.builder().userId(ownerId).visibility(visibility).build();
        ReflectionTestUtils.setField(route, "id", routeId);
        route.getRouteSpots().addAll(List.of(spots));
        return route;
    }

    private RouteSpot routeSpot(Long id, Long spotId, int sequenceOrder) {
        RouteSpot routeSpot = RouteSpot.builder().spotId(spotId).sequenceOrder(sequenceOrder).build();
        ReflectionTestUtils.setField(routeSpot, "id", id);
        return routeSpot;
    }

    private Spot spot(Long id, double lat, double lng) {
        Spot spot = Spot.builder()
                .nameKo("spot" + id)
                .category(SpotCategory.K_HERITAGE)
                .latitude(BigDecimal.valueOf(lat))
                .longitude(BigDecimal.valueOf(lng))
                .build();
        ReflectionTestUtils.setField(spot, "id", id);
        return spot;
    }

    @Test
    void 두_스팟_경로를_계산해_합계와_구간과_폴리라인을_반환한다() {
        Route route = routeWithSpots(Visibility.PRIVATE,
                routeSpot(10L, 101L, 1),
                routeSpot(15L, 105L, 2));
        given(routeRepository.findById(routeId)).willReturn(java.util.Optional.of(route));
        given(spotRepository.findAllById(List.of(101L, 105L)))
                .willReturn(List.of(spot(101L, 37.5, 127.0), spot(105L, 37.6, 127.1)));
        given(tmapDirectionsClient.route(TransportMode.TRANSIT, 127.0, 37.5, 127.1, 37.6))
                .willReturn(new RouteLeg(900, 900, List.of(
                        new double[] {127.0, 37.5}, new double[] {127.1, 37.6})));

        RouteDirectionsResponse response = routeDirectionsService.getDirections(
                routeId, new RouteDirectionsRequest(TransportMode.TRANSIT), ownerId);

        assertThat(response.getTotal().getDistanceMeters()).isEqualTo(900);
        assertThat(response.getTotal().getDistanceText()).isEqualTo("900m");
        assertThat(response.getTotal().getDurationText()).isEqualTo("약 15분");
        assertThat(response.getSegments()).hasSize(1);
        assertThat(response.getSegments().get(0).getDurationText()).isEqualTo("대중교통 15분");
        assertThat(response.getSegments().get(0).getFromRouteSpotId()).isEqualTo(10L);
        assertThat(response.getGeometry().coordinates()).hasSize(2);
    }

    @Test
    void 스팟이_2개_미만이면_예외가_발생한다() {
        Route route = routeWithSpots(Visibility.PRIVATE, routeSpot(10L, 101L, 1));
        given(routeRepository.findById(routeId)).willReturn(java.util.Optional.of(route));

        assertThatThrownBy(() -> routeDirectionsService.getDirections(
                routeId, new RouteDirectionsRequest(TransportMode.WALK), ownerId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DIRECTIONS_NOT_ENOUGH_SPOTS);
    }

    @Test
    void PRIVATE_루트를_타인이_요청하면_예외가_발생한다() {
        Route route = routeWithSpots(Visibility.PRIVATE,
                routeSpot(10L, 101L, 1), routeSpot(15L, 105L, 2));
        given(routeRepository.findById(routeId)).willReturn(java.util.Optional.of(route));

        assertThatThrownBy(() -> routeDirectionsService.getDirections(
                routeId, new RouteDirectionsRequest(TransportMode.CAR), 999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROUTE_FORBIDDEN);
    }

    @Test
    void 동일_요청은_캐시되어_외부_엔진을_다시_호출하지_않는다() {
        Route route = routeWithSpots(Visibility.PUBLIC,
                routeSpot(10L, 101L, 1), routeSpot(15L, 105L, 2));
        given(routeRepository.findById(routeId)).willReturn(java.util.Optional.of(route));
        given(spotRepository.findAllById(List.of(101L, 105L)))
                .willReturn(List.of(spot(101L, 37.5, 127.0), spot(105L, 37.6, 127.1)));
        given(tmapDirectionsClient.route(TransportMode.CAR, 127.0, 37.5, 127.1, 37.6))
                .willReturn(new RouteLeg(1200, 600, List.of(
                        new double[] {127.0, 37.5}, new double[] {127.1, 37.6})));

        RouteDirectionsRequest request = new RouteDirectionsRequest(TransportMode.CAR);
        routeDirectionsService.getDirections(routeId, request, ownerId);
        routeDirectionsService.getDirections(routeId, request, ownerId);

        verify(tmapDirectionsClient, times(1))
                .route(TransportMode.CAR, 127.0, 37.5, 127.1, 37.6);
        verify(tmapDirectionsClient, never())
                .route(org.mockito.ArgumentMatchers.eq(TransportMode.WALK), anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }
}

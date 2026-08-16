package com.Wavey.WaveyService.domain.route.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import com.Wavey.WaveyService.domain.route.dto.request.RouteSpotOrderItem;
import com.Wavey.WaveyService.domain.route.dto.request.RouteSpotReorderRequest;
import com.Wavey.WaveyService.domain.route.dto.response.RouteSpotResponse;
import com.Wavey.WaveyService.domain.route.entity.Route;
import com.Wavey.WaveyService.domain.route.entity.RouteSpot;
import com.Wavey.WaveyService.domain.route.repository.RouteSpotRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RouteSpotServiceTest {

    @Mock
    private RouteSpotRepository routeSpotRepository;

    @Mock
    private RouteService routeService;

    @InjectMocks
    private RouteSpotService routeSpotService;

    private final Long routeId = 1L;
    private final Long ownerId = 100L;
    private Route route;

    @BeforeEach
    void setUp() {
        route = Route.builder().userId(ownerId).build();
        ReflectionTestUtils.setField(route, "id", routeId);
    }

    private RouteSpot routeSpot(Long id, Long spotId, int sequenceOrder) {
        RouteSpot routeSpot = RouteSpot.builder()
                .route(route)
                .spotId(spotId)
                .sequenceOrder(sequenceOrder)
                .build();
        ReflectionTestUtils.setField(routeSpot, "id", id);
        return routeSpot;
    }

    @Test
    void 재정렬_성공시_최신_순서로_정렬된_스팟_목록을_반환한다() {
        List<RouteSpot> currentSpots = List.of(
                routeSpot(10L, 101L, 1),
                routeSpot(11L, 102L, 2)
        );
        List<RouteSpot> reorderedSpots = List.of(
                routeSpot(11L, 102L, 1),
                routeSpot(10L, 101L, 2)
        );
        RouteSpotReorderRequest request = new RouteSpotReorderRequest(List.of(
                new RouteSpotOrderItem(11L, 1),
                new RouteSpotOrderItem(10L, 2)
        ));

        given(routeService.findRouteById(routeId)).willReturn(route);
        given(routeSpotRepository.findByRouteIdOrderBySequenceOrderAsc(routeId))
                .willReturn(currentSpots, reorderedSpots);

        List<RouteSpotResponse> result = routeSpotService.reorderSpots(routeId, request, ownerId);

        assertThat(result).extracting(RouteSpotResponse::getRouteSpotId)
                .containsExactly(11L, 10L);
        verify(routeService).validateOwner(route, ownerId);
    }

    @Test
    void 요청된_스팟_개수가_실제_개수와_다르면_예외가_발생한다() {
        List<RouteSpot> currentSpots = List.of(routeSpot(10L, 101L, 1), routeSpot(11L, 102L, 2));
        RouteSpotReorderRequest request = new RouteSpotReorderRequest(List.of(
                new RouteSpotOrderItem(10L, 1)
        ));

        given(routeService.findRouteById(routeId)).willReturn(route);
        given(routeSpotRepository.findByRouteIdOrderBySequenceOrderAsc(routeId)).willReturn(currentSpots);

        assertThatThrownBy(() -> routeSpotService.reorderSpots(routeId, request, ownerId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROUTE_SPOT_ORDER_MISMATCH);
    }

    @Test
    void 존재하지_않는_라우트_스팟_ID가_포함되면_예외가_발생한다() {
        List<RouteSpot> currentSpots = List.of(routeSpot(10L, 101L, 1));
        RouteSpotReorderRequest request = new RouteSpotReorderRequest(List.of(
                new RouteSpotOrderItem(999L, 1)
        ));

        given(routeService.findRouteById(routeId)).willReturn(route);
        given(routeSpotRepository.findByRouteIdOrderBySequenceOrderAsc(routeId)).willReturn(currentSpots);

        assertThatThrownBy(() -> routeSpotService.reorderSpots(routeId, request, ownerId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROUTE_SPOT_NOT_FOUND);
    }

    @Test
    void 소유자가_아니면_예외가_발생한다() {
        RouteSpotReorderRequest request = new RouteSpotReorderRequest(List.of(
                new RouteSpotOrderItem(10L, 1)
        ));

        given(routeService.findRouteById(routeId)).willReturn(route);
        willThrow(new CustomException(ErrorCode.ROUTE_FORBIDDEN))
                .given(routeService).validateOwner(route, 999L);

        assertThatThrownBy(() -> routeSpotService.reorderSpots(routeId, request, 999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROUTE_FORBIDDEN);
    }
}

package com.Wavey.WaveyService.domain.route.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.Wavey.WaveyService.domain.route.dto.response.RouteSummaryResponse;
import com.Wavey.WaveyService.domain.route.entity.Route;
import com.Wavey.WaveyService.domain.route.entity.Visibility;
import com.Wavey.WaveyService.domain.route.repository.RouteRepository;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private SpotRepository spotRepository;

    @InjectMocks
    private RouteService routeService;

    private final Pageable pageable = PageRequest.of(0, 20);

    @Test
    void regionId가_없으면_전체_공개_루트를_반환한다() {
        Route route = Route.builder().userId(1L).visibility(Visibility.PUBLIC).build();
        given(routeRepository.findByVisibility(Visibility.PUBLIC, pageable))
                .willReturn(new PageImpl<>(List.of(route)));

        Page<RouteSummaryResponse> result = routeService.getPublicRoutes(pageable, null);

        assertThat(result.getContent()).hasSize(1);
        verify(routeRepository, never())
                .findDistinctByVisibilityAndRouteSpots_SpotIdIn(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void regionId가_있으면_해당_지역_스팟을_포함한_루트만_반환한다() {
        Long regionId = 5L;
        Route route = Route.builder().userId(1L).visibility(Visibility.PUBLIC).build();
        given(spotRepository.findIdsByRegionId(regionId)).willReturn(List.of(101L, 102L));
        given(routeRepository.findDistinctByVisibilityAndRouteSpots_SpotIdIn(Visibility.PUBLIC, List.of(101L, 102L), pageable))
                .willReturn(new PageImpl<>(List.of(route)));

        Page<RouteSummaryResponse> result = routeService.getPublicRoutes(pageable, regionId);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void regionId에_해당하는_스팟이_없으면_빈_페이지를_반환한다() {
        Long regionId = 999L;
        given(spotRepository.findIdsByRegionId(regionId)).willReturn(List.of());

        Page<RouteSummaryResponse> result = routeService.getPublicRoutes(pageable, regionId);

        assertThat(result.getContent()).isEmpty();
        verify(routeRepository, never())
                .findDistinctByVisibilityAndRouteSpots_SpotIdIn(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}

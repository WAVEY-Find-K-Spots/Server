package com.Wavey.WaveyService.domain.route.service;

import com.Wavey.WaveyService.domain.route.dto.request.RouteSpotAddRequest;
import com.Wavey.WaveyService.domain.route.dto.request.RouteSpotReorderRequest;
import com.Wavey.WaveyService.domain.route.dto.response.RouteSpotResponse;
import com.Wavey.WaveyService.domain.route.entity.Route;
import com.Wavey.WaveyService.domain.route.entity.RouteSpot;
import com.Wavey.WaveyService.domain.route.repository.RouteSpotRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteSpotService {

    private final RouteSpotRepository routeSpotRepository;
    private final RouteService routeService;

    @Transactional
    public RouteSpotResponse addSpot(Long routeId, RouteSpotAddRequest request, Long userId) {
        Route route = routeService.findRouteById(routeId);
        routeService.validateOwner(route, userId);

        if (routeSpotRepository.existsByRouteIdAndSpotId(routeId, request.getSpotId())) {
            throw new CustomException(ErrorCode.ROUTE_SPOT_ALREADY_EXISTS);
        }

        RouteSpot routeSpot = RouteSpot.builder()
                .route(route)
                .spotId(request.getSpotId())
                .sequenceOrder(request.getSequenceOrder())
                .build();

        return RouteSpotResponse.from(routeSpotRepository.save(routeSpot));
    }

    @Transactional
    public void reorderSpots(Long routeId, RouteSpotReorderRequest request, Long userId) {
        Route route = routeService.findRouteById(routeId);
        routeService.validateOwner(route, userId);

        List<RouteSpot> routeSpots = routeSpotRepository.findByRouteIdOrderBySequenceOrderAsc(routeId);

        Map<Long, RouteSpot> spotMap = routeSpots.stream()
                .collect(Collectors.toMap(RouteSpot::getId, rs -> rs));

        if (spotMap.size() != request.getSpots().size()) {
            throw new CustomException(ErrorCode.ROUTE_SPOT_ORDER_MISMATCH);
        }

        request.getSpots().forEach(item -> {
            RouteSpot routeSpot = spotMap.get(item.getRouteSpotId());
            if (routeSpot == null) {
                throw new CustomException(ErrorCode.ROUTE_SPOT_NOT_FOUND);
            }
            routeSpot.updateSequence(item.getSequenceOrder());
        });
    }

    @Transactional
    public void removeSpot(Long routeId, Long routeSpotId, Long userId) {
        Route route = routeService.findRouteById(routeId);
        routeService.validateOwner(route, userId);
        routeSpotRepository.deleteByRouteIdAndId(routeId, routeSpotId);
    }
}

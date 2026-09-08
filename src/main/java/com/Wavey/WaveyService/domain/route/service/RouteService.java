package com.Wavey.WaveyService.domain.route.service;

import com.Wavey.WaveyService.domain.route.dto.request.RouteCreateRequest;
import com.Wavey.WaveyService.domain.route.dto.request.RouteUpdateRequest;
import com.Wavey.WaveyService.domain.route.dto.response.RouteResponse;
import com.Wavey.WaveyService.domain.route.dto.response.RouteSummaryResponse;
import com.Wavey.WaveyService.domain.route.entity.Route;
import com.Wavey.WaveyService.domain.route.entity.RouteSpot;
import com.Wavey.WaveyService.domain.route.entity.Visibility;
import com.Wavey.WaveyService.domain.route.repository.RouteRepository;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteService {

    private final RouteRepository routeRepository;
    private final RoutePlanningService planning;
    private final SpotRepository spots;

    public List<RouteSummaryResponse> getMyRoutes(Long userId, Visibility visibility) {
        List<Route> routes =
                visibility != null
                        ? routeRepository.findByUserIdAndVisibility(userId, visibility)
                        : routeRepository.findByUserId(userId);

        return routes.stream()
                .sorted(
                        Comparator.comparing(Route::getCreatedAt)
                                .reversed()
                                .thenComparing(Route::getId))
                .map(
                        r -> {
                            var response = RouteSummaryResponse.from(r);
                            response.setPlan(planning.plan(r, userId, r.getTravelMode(), null));
                            return response;
                        })
                .toList();
    }

    public Page<RouteSummaryResponse> getPublicRoutes(Pageable pageable) {
        return routeRepository
                .findByVisibility(Visibility.PUBLIC, pageable)
                .map(RouteSummaryResponse::from);
    }

    public RouteResponse getRoute(Long routeId, Long userId) {
        Route route = findRouteById(routeId);
        validateAccess(route, userId);
        var response = RouteResponse.from(route);
        response.setPlan(planning.plan(route, userId, route.getTravelMode(), null));
        return response;
    }

    @Transactional
    public RouteResponse createRoute(RouteCreateRequest request, Long userId) {
        Route route =
                Route.builder()
                        .userId(userId)
                        .name(request.getName())
                        .description(request.getDescription())
                        .visibility(request.getVisibility())
                        .build();

        if (request.getSpots() != null) {
            var ids = new HashSet<Long>();
            var orders = new HashSet<Integer>();
            for (var item : request.getSpots()) {
                if (!spots.existsById(item.getSpotId()))
                    throw new CustomException(ErrorCode.SPOT_NOT_FOUND);
                if (!ids.add(item.getSpotId()))
                    throw new CustomException(ErrorCode.ROUTE_SPOT_ALREADY_EXISTS);
                if (!orders.add(item.getSequenceOrder()))
                    throw new CustomException(ErrorCode.ROUTE_SPOT_ORDER_MISMATCH);
            }
            request.getSpots()
                    .forEach(
                            spotRequest -> {
                                RouteSpot routeSpot =
                                        RouteSpot.builder()
                                                .route(route)
                                                .spotId(spotRequest.getSpotId())
                                                .sequenceOrder(spotRequest.getSequenceOrder())
                                                .build();
                                route.getRouteSpots().add(routeSpot);
                            });
        }

        return RouteResponse.from(routeRepository.save(route));
    }

    @Transactional
    public RouteResponse updateRoute(Long routeId, RouteUpdateRequest request, Long userId) {
        Route route = findRouteById(routeId);
        validateOwner(route, userId);
        route.update(request.getName(), request.getDescription(), request.getVisibility());
        return RouteResponse.from(route);
    }

    @Transactional
    public void deleteRoute(Long routeId, Long userId) {
        Route route = findRouteById(routeId);
        validateOwner(route, userId);
        routeRepository.delete(route);
    }

    public Route findRouteById(Long routeId) {
        return routeRepository
                .findById(routeId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTE_NOT_FOUND));
    }

    public void validateOwner(Route route, Long userId) {
        if (!route.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ROUTE_FORBIDDEN);
        }
    }

    private void validateAccess(Route route, Long userId) {
        if (route.getVisibility() == Visibility.PRIVATE && !route.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ROUTE_FORBIDDEN);
        }
    }
}

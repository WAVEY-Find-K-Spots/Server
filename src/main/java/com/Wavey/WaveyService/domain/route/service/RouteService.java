package com.Wavey.WaveyService.domain.route.service;

import com.Wavey.WaveyService.domain.route.dto.request.RouteCreateRequest;
import com.Wavey.WaveyService.domain.route.dto.request.RouteUpdateRequest;
import com.Wavey.WaveyService.domain.route.dto.response.RouteResponse;
import com.Wavey.WaveyService.domain.route.dto.response.RouteSummaryResponse;
import com.Wavey.WaveyService.domain.route.entity.Route;
import com.Wavey.WaveyService.domain.route.entity.RouteSpot;
import com.Wavey.WaveyService.domain.route.entity.Visibility;
import com.Wavey.WaveyService.domain.route.repository.RouteRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteService {

    private final RouteRepository routeRepository;

    public List<RouteSummaryResponse> getMyRoutes(Long userId, Visibility visibility) {
        List<Route> routes = visibility != null
                ? routeRepository.findByUserIdAndVisibility(userId, visibility)
                : routeRepository.findByUserId(userId);

        return routes.stream()
                .map(RouteSummaryResponse::from)
                .toList();
    }

    public Page<RouteSummaryResponse> getPublicRoutes(Pageable pageable) {
        return routeRepository.findByVisibility(Visibility.PUBLIC, pageable)
                .map(RouteSummaryResponse::from);
    }

    public RouteResponse getRoute(Long routeId, Long userId) {
        Route route = findRouteById(routeId);
        validateAccess(route, userId);
        return RouteResponse.from(route);
    }

    @Transactional
    public RouteResponse createRoute(RouteCreateRequest request, Long userId) {
        Route route = Route.builder()
                .userId(userId)
                .name(request.getName())
                .description(request.getDescription())
                .visibility(request.getVisibility())
                .build();

        if (request.getSpots() != null) {
            request.getSpots().forEach(spotRequest -> {
                RouteSpot routeSpot = RouteSpot.builder()
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
        return routeRepository.findById(routeId)
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
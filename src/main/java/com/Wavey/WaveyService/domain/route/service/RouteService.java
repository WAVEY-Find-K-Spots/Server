package com.Wavey.WaveyService.domain.route.service;

import com.Wavey.WaveyService.domain.route.dto.RouteRequest;
import com.Wavey.WaveyService.domain.route.dto.RouteResponse;
import com.Wavey.WaveyService.domain.route.entity.Route;
import com.Wavey.WaveyService.domain.route.repository.RouteRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;

    @Transactional
    public RouteResponse createRoute(RouteRequest request) {
        Route route = Route.builder()
                .user_id(request.getUser_id())
                .title(request.getTitle())
                .description(request.getDescription())
                .progress(request.getProgress())
                .build();

        return convertToResponse(routeRepository.save(route));
    }

    @Transactional(readOnly = true)
    public List<RouteResponse> getAllRoutes() {
        return routeRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RouteResponse getRouteById(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTE_NOT_FOUND));
        return convertToResponse(route);
    }

    @Transactional
    public RouteResponse updateRoute(Long id, RouteRequest request) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTE_NOT_FOUND));

        route.update(request.getTitle(), request.getDescription(), request.getProgress());
        return convertToResponse(route);
    }

    @Transactional
    public void deleteRoute(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTE_NOT_FOUND));
        routeRepository.delete(route);
    }

    private RouteResponse convertToResponse(Route route) {
        return RouteResponse.builder()
                .route_id(route.getRoute_id())
                .user_id(route.getUser_id())
                .title(route.getTitle())
                .description(route.getDescription())
                .progress(route.getProgress())
                .created_at(route.getCreated_at())
                .updated_at(route.getUpdated_at())
                .build();
    }
}
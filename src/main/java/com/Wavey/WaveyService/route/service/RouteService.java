package com.Wavey.WaveyService.route.service;

import com.Wavey.WaveyService.route.dto.RouteRequest;
import com.Wavey.WaveyService.route.dto.RouteResponse;
import com.Wavey.WaveyService.route.entity.Route;
import com.Wavey.WaveyService.route.repository.RouteRepository;
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
                .orElseThrow(() -> new IllegalArgumentException("해당 루트가 없습니다. id=" + id));
        return convertToResponse(route);
    }

    @Transactional
    public RouteResponse updateRoute(Long id, RouteRequest request) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 루트가 없습니다. id=" + id));

        route.update(request.getTitle(), request.getDescription(), request.getProgress());
        return convertToResponse(route);
    }

    @Transactional
    public void deleteRoute(Long id) {
        routeRepository.deleteById(id);
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
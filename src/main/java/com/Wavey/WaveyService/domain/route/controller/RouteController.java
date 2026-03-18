package com.Wavey.WaveyService.domain.route.controller;

import com.Wavey.WaveyService.domain.route.dto.RouteRequest;
import com.Wavey.WaveyService.domain.route.dto.RouteResponse;
import com.Wavey.WaveyService.domain.route.service.RouteService;
import com.Wavey.WaveyService.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @PostMapping
    public ResponseEntity<CommonResponse<RouteResponse>> create(@RequestBody RouteRequest request) {
        return ResponseEntity.ok(CommonResponse.success("루트 생성 성공", routeService.createRoute(request)));
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<RouteResponse>>> getAll() {
        return ResponseEntity.ok(CommonResponse.success("루트 목록 조회 성공", routeService.getAllRoutes()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<RouteResponse>> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(CommonResponse.success("루트 상세 조회 성공", routeService.getRouteById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<RouteResponse>> update(@PathVariable Long id, @RequestBody RouteRequest request) {
        return ResponseEntity.ok(CommonResponse.success("루트 수정 성공", routeService.updateRoute(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> delete(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.ok(CommonResponse.success("루트 삭제 성공", null));
    }
}
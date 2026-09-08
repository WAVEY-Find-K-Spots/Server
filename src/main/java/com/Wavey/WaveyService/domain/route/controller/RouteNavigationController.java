package com.Wavey.WaveyService.domain.route.controller;

import com.Wavey.WaveyService.domain.route.entity.RouteLeg.TravelMode;
import com.Wavey.WaveyService.domain.route.service.RoutePlanningService;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.global.response.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/routes/{routeId}")
public class RouteNavigationController {
    private final RoutePlanningService service;

    public record ModeRequest(@NotNull TravelMode mode) {}

    public record Step(
            @Min(0) int expectedIndex,
            @NotNull Long expectedVersion,
            @NotNull Direction direction) {}

    public enum Direction {
        NEXT,
        PREVIOUS
    }

    @GetMapping("/plan")
    public ApiResponse<RoutePlanningService.Plan> plan(
            @PathVariable Long routeId,
            @AuthenticationPrincipal User u,
            @RequestParam(required = false) TravelMode mode,
            @RequestParam(required = false) String language) {
        return ApiResponse.success("경로 정보", service.plan(routeId, u.getId(), mode, language));
    }

    @PatchMapping("/travel-mode")
    public ApiResponse<RoutePlanningService.Plan> mode(
            @PathVariable Long routeId,
            @AuthenticationPrincipal User u,
            @Valid @RequestBody ModeRequest r,
            @RequestParam(required = false) String language) {
        return ApiResponse.success(
                "이동 수단 저장", service.setMode(routeId, u.getId(), r.mode(), language));
    }

    @PostMapping("/navigation")
    public ApiResponse<RoutePlanningService.Progress> start(
            @PathVariable Long routeId,
            @AuthenticationPrincipal User u,
            @RequestParam(required = false) TravelMode mode,
            @RequestParam(required = false) String language) {
        return ApiResponse.success("경로 탐색 시작", service.start(routeId, u.getId(), mode, language));
    }

    @GetMapping("/navigation")
    public ApiResponse<RoutePlanningService.Progress> current(
            @PathVariable Long routeId,
            @AuthenticationPrincipal User u,
            @RequestParam(required = false) String language) {
        return ApiResponse.success("경로 진행", service.current(routeId, u.getId(), language));
    }

    @PatchMapping("/navigation")
    public ApiResponse<RoutePlanningService.Progress> step(
            @PathVariable Long routeId,
            @AuthenticationPrincipal User u,
            @Valid @RequestBody Step r,
            @RequestParam(required = false) String language) {
        return ApiResponse.success(
                "경로 진행 저장",
                service.step(
                        routeId,
                        u.getId(),
                        r.expectedIndex(),
                        r.expectedVersion(),
                        r.direction() == Direction.NEXT,
                        language));
    }
}

package com.Wavey.WaveyService.domain.route.controller;

import com.Wavey.WaveyService.domain.route.directions.service.RouteDirectionsService;
import com.Wavey.WaveyService.domain.route.dto.request.RouteDirectionsRequest;
import com.Wavey.WaveyService.domain.route.dto.response.RouteDirectionsResponse;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "RouteDirections", description = "루트 경로 계산 API")
@RestController
@RequestMapping("/api/v1/routes/{routeId}/directions")
@RequiredArgsConstructor
public class RouteDirectionsController {

    private final RouteDirectionsService routeDirectionsService;

    @Operation(summary = "루트 경로 계산", description = "저장된 루트의 스팟 순서를 기준으로 이동수단별 경로(총 거리·시간, 구간별 소요시간, 폴리라인)를 계산합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계산 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "스팟 부족 / 지원하지 않는 이동수단"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "루트를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "외부 경로 엔진 오류")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<RouteDirectionsResponse>> getDirections(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "루트 ID") @PathVariable Long routeId,
            @RequestBody @Valid RouteDirectionsRequest request
    ) {
        Long userId = extractUserId(user);
        return ResponseEntity.ok(
                ApiResponse.success("경로 계산 성공", routeDirectionsService.getDirections(routeId, request, userId)));
    }

    // TODO: 로컬 개발용 — 인증 복구 시 null 분기를 지우고 user.getId()만 사용
    private Long extractUserId(User user) {
        if (user == null) {
            return 1L;
        }
        return user.getId();
    }
}

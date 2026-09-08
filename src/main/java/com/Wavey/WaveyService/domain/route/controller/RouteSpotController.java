package com.Wavey.WaveyService.domain.route.controller;

import com.Wavey.WaveyService.domain.route.dto.request.RouteSpotAddRequest;
import com.Wavey.WaveyService.domain.route.dto.request.RouteSpotReorderRequest;
import com.Wavey.WaveyService.domain.route.dto.response.RouteSpotResponse;
import com.Wavey.WaveyService.domain.route.service.RouteSpotService;
import com.Wavey.WaveyService.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "RouteSpot", description = "루트 스팟 관리 API")
@RestController
@RequestMapping("/api/v1/routes/{routeId}/spots")
@RequiredArgsConstructor
public class RouteSpotController {

    private final RouteSpotService routeSpotService;

    @Operation(summary = "루트에 스팟 추가", description = "기존 루트에 스팟을 추가합니다. 이미 추가된 스팟이면 409를 반환합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<RouteSpotResponse>> addSpot(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "루트 ID") @PathVariable Long routeId,
            @RequestBody @Valid RouteSpotAddRequest request
    ) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "스팟 추가 성공", routeSpotService.addSpot(routeId, request, userId)));
    }

    @Operation(summary = "스팟 순서 일괄 변경", description = "루트 내 스팟 전체 순서를 한 번에 재정렬합니다.")
    @PatchMapping("/reorder")
    public ResponseEntity<ApiResponse<List<RouteSpotResponse>>> reorderSpots(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "루트 ID") @PathVariable Long routeId,
            @RequestBody @Valid RouteSpotReorderRequest request
    ) {
        Long userId = extractUserId(userDetails);
        List<RouteSpotResponse> response = routeSpotService.reorderSpots(routeId, request, userId);
        return ResponseEntity.ok(ApiResponse.success("스팟 순서 변경 성공", response));
    }

    @Operation(summary = "루트에서 스팟 제거", description = "루트에서 특정 스팟을 제거합니다. 스팟 자체는 삭제되지 않습니다.")
    @DeleteMapping("/{routeSpotId}")
    public ResponseEntity<ApiResponse<Void>> removeSpot(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "루트 ID") @PathVariable Long routeId,
            @Parameter(description = "루트 스팟 ID") @PathVariable Long routeSpotId
    ) {
        Long userId = extractUserId(userDetails);
        routeSpotService.removeSpot(routeId, routeSpotId, userId);
        return ResponseEntity.ok(ApiResponse.success("스팟 제거 성공", null));
    }

    private Long extractUserId(UserDetails userDetails) {
        // TODO: 로컬 개발용 — 인증 복구 시 null 분기를 지우고 userDetails만 사용
        if (userDetails == null) {
            return 1L;
        }
        return Long.parseLong(userDetails.getUsername());
    }
}

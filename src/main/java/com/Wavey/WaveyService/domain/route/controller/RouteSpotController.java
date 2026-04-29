package com.Wavey.WaveyService.domain.route.controller;

import com.Wavey.WaveyService.domain.route.dto.request.RouteSpotAddRequest;
import com.Wavey.WaveyService.domain.route.dto.request.RouteSpotReorderRequest;
import com.Wavey.WaveyService.domain.route.dto.response.RouteSpotResponse;
import com.Wavey.WaveyService.domain.route.service.RouteSpotService;
import com.Wavey.WaveyService.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "RouteSpot", description = "루트 스팟 관리 API")
@RestController
@RequestMapping("/api/v1/routes/{routeId}/spots")
@RequiredArgsConstructor
public class RouteSpotController {

    private final RouteSpotService routeSpotService;

    @Operation(summary = "루트에 스팟 추가", description = "기존 루트에 스팟을 추가합니다. 이미 추가된 스팟이면 409를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "추가 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "루트를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 추가된 스팟")
    })
    @PostMapping
    public ResponseEntity<CommonResponse<RouteSpotResponse>> addSpot(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "루트 ID") @PathVariable Long routeId,
            @RequestBody @Valid RouteSpotAddRequest request
    ) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("스팟 추가 성공", routeSpotService.addSpot(routeId, request, userId)));
    }

    @Operation(summary = "스팟 순서 일괄 변경", description = "루트 내 스팟 전체 순서를 한 번에 재정렬합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재정렬 성공"),
            @ApiResponse(responseCode = "400", description = "스팟 ID 누락"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "루트를 찾을 수 없음")
    })
    @PatchMapping("/reorder")
    public ResponseEntity<CommonResponse<Void>> reorderSpots(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "루트 ID") @PathVariable Long routeId,
            @RequestBody @Valid RouteSpotReorderRequest request
    ) {
        Long userId = extractUserId(userDetails);
        routeSpotService.reorderSpots(routeId, request, userId);
        return ResponseEntity.ok(CommonResponse.success("스팟 순서 변경 성공", null));
    }

    @Operation(summary = "루트에서 스팟 제거", description = "루트에서 특정 스팟을 제거합니다. 스팟 자체는 삭제되지 않습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "제거 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "루트 또는 스팟을 찾을 수 없음")
    })
    @DeleteMapping("/{routeSpotId}")
    public ResponseEntity<CommonResponse<Void>> removeSpot(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "루트 ID") @PathVariable Long routeId,
            @Parameter(description = "루트 스팟 ID") @PathVariable Long routeSpotId
    ) {
        Long userId = extractUserId(userDetails);
        routeSpotService.removeSpot(routeId, routeSpotId, userId);
        return ResponseEntity.ok(CommonResponse.success("스팟 제거 성공", null));
    }

    private Long extractUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}

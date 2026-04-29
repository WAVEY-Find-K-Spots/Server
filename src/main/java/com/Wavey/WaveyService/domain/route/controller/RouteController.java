package com.Wavey.WaveyService.domain.route.controller;

import com.Wavey.WaveyService.domain.route.dto.request.RouteCreateRequest;
import com.Wavey.WaveyService.domain.route.dto.request.RouteUpdateRequest;
import com.Wavey.WaveyService.domain.route.dto.response.RouteResponse;
import com.Wavey.WaveyService.domain.route.dto.response.RouteSummaryResponse;
import com.Wavey.WaveyService.domain.route.entity.Visibility;
import com.Wavey.WaveyService.domain.route.service.RouteService;
import com.Wavey.WaveyService.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Route", description = "루트 관리 API")
@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @Operation(summary = "내 루트 목록 조회", description = "로그인한 사용자의 루트 목록을 반환합니다. visibility로 필터링 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<CommonResponse<List<RouteSummaryResponse>>> getMyRoutes(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "공개 여부 필터 (PUBLIC / PRIVATE)") @RequestParam(required = false) Visibility visibility
    ) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(CommonResponse.success("내 루트 목록 조회 성공", routeService.getMyRoutes(userId, visibility)));
    }

    @Operation(summary = "공개 루트 전체 조회", description = "모든 사용자의 공개 루트를 조회합니다. 인증 없이 접근 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/public")
    public ResponseEntity<CommonResponse<Page<RouteSummaryResponse>>> getPublicRoutes(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(CommonResponse.success("공개 루트 조회 성공", routeService.getPublicRoutes(pageable)));
    }

    @Operation(summary = "루트 상세 조회", description = "루트 상세 정보 및 포함된 스팟 목록을 반환합니다. PRIVATE 루트는 본인만 조회 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "루트를 찾을 수 없음")
    })
    @GetMapping("/{routeId}")
    public ResponseEntity<CommonResponse<RouteResponse>> getRoute(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "루트 ID") @PathVariable Long routeId
    ) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(CommonResponse.success("루트 상세 조회 성공", routeService.getRoute(routeId, userId)));
    }

    @Operation(summary = "루트 생성", description = "새로운 루트를 생성합니다. 스팟은 생략 시 빈 루트로 생성됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping
    public ResponseEntity<CommonResponse<RouteResponse>> createRoute(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid RouteCreateRequest request
    ) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("루트 생성 성공", routeService.createRoute(request, userId)));
    }

    @Operation(summary = "루트 수정", description = "루트의 이름, 설명, 공개 여부를 수정합니다. 본인의 루트만 수정 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "루트를 찾을 수 없음")
    })
    @PatchMapping("/{routeId}")
    public ResponseEntity<CommonResponse<RouteResponse>> updateRoute(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "루트 ID") @PathVariable Long routeId,
            @RequestBody @Valid RouteUpdateRequest request
    ) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(CommonResponse.success("루트 수정 성공", routeService.updateRoute(routeId, request, userId)));
    }

    @Operation(summary = "루트 삭제", description = "루트 및 연결된 스팟 데이터를 함께 삭제합니다. 본인의 루트만 삭제 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "루트를 찾을 수 없음")
    })
    @DeleteMapping("/{routeId}")
    public ResponseEntity<CommonResponse<Void>> deleteRoute(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "루트 ID") @PathVariable Long routeId
    ) {
        Long userId = extractUserId(userDetails);
        routeService.deleteRoute(routeId, userId);
        return ResponseEntity.ok(CommonResponse.success("루트 삭제 성공", null));
    }

    private Long extractUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}
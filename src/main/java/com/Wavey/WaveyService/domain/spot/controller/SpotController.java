package com.Wavey.WaveyService.domain.spot.controller;

import static com.Wavey.WaveyService.global.response.CommonResponse.success;

import com.Wavey.WaveyService.domain.spot.dto.request.SpotCreateRequest;
import com.Wavey.WaveyService.domain.spot.dto.request.SpotUpdateRequest;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotListResponse;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotResponse;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.service.SpotDetailService;
import com.Wavey.WaveyService.domain.spot.service.SpotDiscoveryService;
import com.Wavey.WaveyService.domain.spot.service.SpotService;
import com.Wavey.WaveyService.global.response.CommonResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Spot", description = "스팟(장소) 관리 API")
@Validated
@RestController
@RequestMapping("/api/v1/spots")
@RequiredArgsConstructor
public class SpotController {

    private final SpotService spotService;

    @Operation(summary = "장소 생성", description = "새로운 장소를 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "409", description = "중복된 외부 장소 데이터")
    })
    @PostMapping
    public ResponseEntity<CommonResponse<SpotResponse>> createSpot(
            @Valid @RequestBody SpotCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success(HttpStatus.CREATED.value(), "장소 생성 성공", spotService.createSpot(request)));
    }

    @Operation(summary = "장소 단건 조회", description = "spotId로 장소 1건을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "장소를 찾을 수 없음")
    })
    @GetMapping("/{spotId}")
    public ResponseEntity<CommonResponse<SpotResponse>> getSpot(
            @Parameter(description = "장소 ID") @PathVariable Long spotId
    ) {
        return ResponseEntity.ok(success("장소 단건 조회 성공", spotService.getSpot(spotId)));
    }

    @Operation(summary = "장소 목록 조회", description = "category, regionId 필터를 선택적으로 사용해 장소 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<CommonResponse<List<SpotListResponse>>> getSpots(
            @Parameter(description = "장소 카테고리 필터") @RequestParam(required = false) SpotCategory category,
            @Parameter(description = "지역 ID 필터") @RequestParam(required = false) Long regionId
    ) {
        return ResponseEntity.ok(success("장소 목록 조회 성공", spotService.getSpots(category, regionId)));
    }

    @Operation(summary = "지도 범위 장소 조회", description = "지도 좌표 범위(min/max 위경도)에 포함되는 장소를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 지도 범위")
    })
    @GetMapping("/map")
    public ResponseEntity<CommonResponse<List<SpotListResponse>>> getSpotsInMapBounds(
            @Parameter(description = "최소 위도")
            @RequestParam @NotNull @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") BigDecimal minLat,
            @Parameter(description = "최대 위도")
            @RequestParam @NotNull @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") BigDecimal maxLat,
            @Parameter(description = "최소 경도")
            @RequestParam @NotNull @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") BigDecimal minLng,
            @Parameter(description = "최대 경도")
            @RequestParam @NotNull @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") BigDecimal maxLng
    ) {
        return ResponseEntity.ok(success("지도 범위 장소 조회 성공", spotService.getSpotsByMapBounds(minLat, maxLat, minLng, maxLng)));
    }

    @Operation(summary = "장소 수정", description = "spotId에 해당하는 장소 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "장소를 찾을 수 없음")
    })
    @PatchMapping("/{spotId}")
    public ResponseEntity<CommonResponse<SpotResponse>> updateSpot(
            @Parameter(description = "장소 ID") @PathVariable Long spotId,
            @Valid @RequestBody SpotUpdateRequest request
    ) {
        return ResponseEntity.ok(success("장소 수정 성공", spotService.updateSpot(spotId, request)));
    }

    @Operation(summary = "장소 삭제", description = "spotId에 해당하는 장소를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "장소를 찾을 수 없음")
    })
    @DeleteMapping("/{spotId}")
    public ResponseEntity<CommonResponse<Void>> deleteSpot(
            @Parameter(description = "장소 ID") @PathVariable Long spotId
    ) {
        spotService.deleteSpot(spotId);
        return ResponseEntity.ok(success("장소 삭제 성공", null));
    }
}

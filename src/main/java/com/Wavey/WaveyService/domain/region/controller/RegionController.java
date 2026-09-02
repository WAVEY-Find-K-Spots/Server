package com.Wavey.WaveyService.domain.region.controller;

import static com.Wavey.WaveyService.global.response.ApiResponse.success;

import com.Wavey.WaveyService.domain.region.dto.request.RegionCreateRequest;
import com.Wavey.WaveyService.domain.region.dto.request.RegionUpdateRequest;
import com.Wavey.WaveyService.domain.region.dto.response.RegionResponse;
import com.Wavey.WaveyService.domain.region.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Region", description = "지역 관리 API")
@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    @Operation(summary = "지역 생성", description = "스팟에서 사용할 지역 정보를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "409", description = "중복된 지역 코드")
    })
    @PostMapping
    public ResponseEntity<com.Wavey.WaveyService.global.response.ApiResponse<RegionResponse>> createRegion(@Valid @RequestBody RegionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success(HttpStatus.CREATED.value(), "지역 생성 성공", regionService.createRegion(request)));
    }

    @Operation(summary = "지역 단건 조회", description = "regionId로 지역 1건을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "지역을 찾을 수 없음")
    })
    @GetMapping("/{regionId}")
    public ResponseEntity<com.Wavey.WaveyService.global.response.ApiResponse<RegionResponse>> getRegion(
            @Parameter(description = "지역 ID") @PathVariable Long regionId
    ) {
        return ResponseEntity.ok(success("지역 단건 조회 성공", regionService.getRegion(regionId)));
    }

    @Operation(summary = "지역 목록 조회", description = "등록된 전체 지역 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<com.Wavey.WaveyService.global.response.ApiResponse<List<RegionResponse>>> getRegions() {
        return ResponseEntity.ok(success("지역 목록 조회 성공", regionService.getRegions()));
    }

    @Operation(summary = "지역 수정", description = "regionId에 해당하는 지역 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "지역을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "중복된 지역 코드")
    })
    @PatchMapping("/{regionId}")
    public ResponseEntity<com.Wavey.WaveyService.global.response.ApiResponse<RegionResponse>> updateRegion(
            @Parameter(description = "지역 ID") @PathVariable Long regionId,
            @Valid @RequestBody RegionUpdateRequest request
    ) {
        return ResponseEntity.ok(success("지역 수정 성공", regionService.updateRegion(regionId, request)));
    }

    @Operation(summary = "지역 삭제", description = "regionId에 해당하는 지역을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "지역을 찾을 수 없음")
    })
    @DeleteMapping("/{regionId}")
    public ResponseEntity<com.Wavey.WaveyService.global.response.ApiResponse<Void>> deleteRegion(
            @Parameter(description = "지역 ID") @PathVariable Long regionId
    ) {
        regionService.deleteRegion(regionId);
        return ResponseEntity.ok(success("지역 삭제 성공", null));
    }
}

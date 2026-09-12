package com.Wavey.WaveyService.domain.spot.controller;

import static com.Wavey.WaveyService.global.response.CommonResponse.success;

import com.Wavey.WaveyService.domain.spot.dto.request.SpotCreateRequest;
import com.Wavey.WaveyService.domain.spot.dto.request.SpotSearchRequest;
import com.Wavey.WaveyService.domain.spot.dto.request.SpotUpdateRequest;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotNearbyResponse;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotPageResponse;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotResponse;
import com.Wavey.WaveyService.domain.spot.service.SpotNearbyService;
import com.Wavey.WaveyService.domain.spot.service.SpotSearchService;
import com.Wavey.WaveyService.domain.spot.service.SpotService;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.global.response.CommonResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Spot", description = "장소 관리 및 검색 API")
@RestController
@RequestMapping("/api/v1/spots")
@RequiredArgsConstructor
@Validated
@PreAuthorize("isAuthenticated()")
public class SpotController {

    private final SpotService spotService;
    private final SpotSearchService spotSearchService;
    private final SpotNearbyService spotNearbyService;

    @Operation(
            summary = "장소 생성",
            description = "관리자가 장소 정보를 생성합니다. category, placeType은 Swagger에 표시된 enum 값만 사용할 수 있습니다.",
            security = @SecurityRequirement(name = "JWT"))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = SpotResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<SpotResponse>> createSpot(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "장소 생성 요청",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SpotCreateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "regionId": 1,
                                      "nameKo": "경복궁",
                                      "nameEn": "Gyeongbokgung Palace",
                                      "category": "K_HERITAGE",
                                      "placeType": "OTHER",
                                      "descriptionKo": "조선 시대 대표 궁궐입니다.",
                                      "descriptionEn": "A representative palace of the Joseon dynasty.",
                                      "openingHours": "09:00-18:00",
                                      "breakTime": null,
                                      "closedDaysKo": "화요일",
                                      "closedDaysEn": "Tuesday",
                                      "tel": "02-3700-3900",
                                      "addressKo": "서울특별시 종로구 사직로 161",
                                      "addressEn": "161 Sajik-ro, Jongno-gu, Seoul",
                                      "transportInfoKo": "지하철 3호선 경복궁역 5번 출구",
                                      "transportInfoEn": "Exit 5 of Gyeongbokgung Station, Line 3",
                                      "latitude": 37.579617,
                                      "longitude": 126.977041,
                                      "imageUrl": "https://example.com/spots/gyeongbokgung.jpg"
                                    }
                                    """)))
            @Valid @RequestBody
            SpotCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        success(
                                HttpStatus.CREATED.value(),
                                "장소 생성 성공",
                                spotService.createSpot(request)
                        )
                );
    }

    @Operation(
            summary = "장소 목록 검색",
            description = "키워드, 지역, 카테고리, 장소 타입, 평점, 위치 반경, 정렬 조건으로 장소 목록을 검색합니다. 좌표 기반 검색은 latitude와 longitude를 함께 전달합니다.",
            security = @SecurityRequirement(name = "JWT"))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = SpotPageResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<CommonResponse<SpotPageResponse>> getSpots(
            @ParameterObject @Valid @ModelAttribute
            SpotSearchRequest request,

            @Parameter(hidden = true) @AuthenticationPrincipal
            User user
    ) {
        return ResponseEntity.ok(
                success(
                        "장소 목록 조회 성공",
                        spotSearchService.search(
                                request,
                                userId(user)
                        )
                )
        );
    }

    @Operation(
            summary = "장소 단건 조회",
            description = "spotId에 해당하는 장소 상세 정보를 조회합니다.",
            security = @SecurityRequirement(name = "JWT"))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = SpotResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "장소를 찾을 수 없음")
    })
    @GetMapping("/{spotId}")
    public ResponseEntity<CommonResponse<SpotResponse>> getSpot(
            @Parameter(description = "장소 ID", example = "1") @PathVariable
            Long spotId,

            @Parameter(hidden = true) @AuthenticationPrincipal
            User user
    ) {
        return ResponseEntity.ok(
                success(
                        "장소 단건 조회 성공",
                        spotService.getSpot(
                                spotId,
                                userId(user)
                        )
                )
        );
    }

    @Operation(
            summary = "주변 장소 조회",
            description = "기준 장소와 지정한 반경 안에 있는 주변 장소를 조회합니다. radiusMeters는 1.0 이상 100000.0 이하입니다.",
            security = @SecurityRequirement(name = "JWT"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "장소를 찾을 수 없음")
    })
    @GetMapping("/{spotId}/nearby")
    public ResponseEntity<CommonResponse<List<SpotNearbyResponse>>> getNearbySpots(
            @Parameter(description = "기준 장소 ID", example = "1") @PathVariable
            Long spotId,

            @Parameter(description = "검색 반경(미터)", example = "5000.0")
            @RequestParam(defaultValue = "5000")
            @DecimalMin("1.0")
            @DecimalMax("100000.0")
            double radiusMeters
    ) {
        return ResponseEntity.ok(
                success(
                        "주변 장소 조회 성공",
                        spotNearbyService.findNearby(
                                spotId,
                                radiusMeters
                        )
                )
        );
    }

    @Operation(
            summary = "장소 수정",
            description = "관리자가 spotId에 해당하는 장소 정보를 수정합니다. 요청한 필드만 수정합니다.",
            security = @SecurityRequirement(name = "JWT"))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = SpotResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @ApiResponse(responseCode = "404", description = "장소를 찾을 수 없음")
    })
    @PatchMapping("/{spotId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<SpotResponse>> updateSpot(
            @Parameter(description = "장소 ID", example = "1") @PathVariable
            Long spotId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "장소 수정 요청",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SpotUpdateRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "regionId": 1,
                                      "nameKo": "경복궁",
                                      "nameEn": "Gyeongbokgung Palace",
                                      "category": "K_HERITAGE",
                                      "placeType": "OTHER",
                                      "descriptionKo": "조선 시대 대표 궁궐입니다.",
                                      "descriptionEn": "A representative palace of the Joseon dynasty.",
                                      "openingHours": "09:00-18:00",
                                      "breakTime": null,
                                      "closedDaysKo": "화요일",
                                      "closedDaysEn": "Tuesday",
                                      "tel": "02-3700-3900",
                                      "addressKo": "서울특별시 종로구 사직로 161",
                                      "addressEn": "161 Sajik-ro, Jongno-gu, Seoul",
                                      "transportInfoKo": "지하철 3호선 경복궁역 5번 출구",
                                      "transportInfoEn": "Exit 5 of Gyeongbokgung Station, Line 3",
                                      "latitude": 37.579617,
                                      "longitude": 126.977041,
                                      "imageUrl": "https://example.com/spots/gyeongbokgung.jpg"
                                    }
                                    """)))
            @Valid @RequestBody
            SpotUpdateRequest request
    ) {
        return ResponseEntity.ok(
                success(
                        "장소 수정 성공",
                        spotService.updateSpot(
                                spotId,
                                request
                        )
                )
        );
    }

    @Operation(
            summary = "장소 삭제",
            description = "관리자가 spotId에 해당하는 장소를 삭제합니다.",
            security = @SecurityRequirement(name = "JWT"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @ApiResponse(responseCode = "404", description = "장소를 찾을 수 없음")
    })
    @DeleteMapping("/{spotId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Void>> deleteSpot(
            @Parameter(description = "장소 ID", example = "1") @PathVariable
            Long spotId
    ) {
        spotService.deleteSpot(spotId);

        return ResponseEntity.ok(
                success(
                        "장소 삭제 성공",
                        null
                )
        );
    }

    private Long userId(User user) {
        return user == null
                ? null
                : user.getId();
    }
}

package com.Wavey.WaveyService.domain.spot.controller;

import static com.Wavey.WaveyService.global.response.ApiResponse.success;

import com.Wavey.WaveyService.domain.spot.dto.response.SpotSyncResponse;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.external.service.SpotExternalSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Spot Sync", description = "외부 공공데이터 장소 동기화 API")
@RestController
@RequestMapping("/api/v1/spots/sync")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SpotSyncController {

    private final SpotExternalSyncService spotExternalSyncService;

    @Operation(
            summary = "한국관광공사 문화유산 장소 페이지 동기화",
            description = "한국관광공사 국문 관광정보 서비스에서 지정한 페이지의 관광지/역사관광 데이터를 조회해 K_HERITAGE 장소로 저장하거나 변경된 필드만 갱신합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "동기화 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 인증키 누락"),
            @ApiResponse(responseCode = "502", description = "외부 API 요청 실패")
    })
    @PostMapping("/heritage")
    public ResponseEntity<com.Wavey.WaveyService.global.response.ApiResponse<SpotSyncResponse>> syncHeritageSpots(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "페이지당 조회 개수") @RequestParam(defaultValue = "100") int numOfRows
    ) {
        return ResponseEntity.ok(success(
                "한국관광공사 문화유산 장소 페이지 동기화 성공",
                spotExternalSyncService.syncHeritageSpots(pageNo, numOfRows)
        ));
    }

    @Operation(
            summary = "한국관광공사 문화유산 장소 전체 동기화",
            description = "한국관광공사 국문 관광정보 서비스의 문화유산 장소를 전체 페이지 순회로 동기화합니다. 기존 데이터는 sourceType과 externalContentId 기준으로 비교해 변경된 필드만 갱신합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 동기화 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 인증키 누락"),
            @ApiResponse(responseCode = "502", description = "외부 API 요청 실패")
    })
    @PostMapping("/heritage/all")
    public ResponseEntity<com.Wavey.WaveyService.global.response.ApiResponse<SpotSyncResponse>> syncAllHeritageSpots(
            @Parameter(description = "페이지당 조회 개수") @RequestParam(defaultValue = "100") int numOfRows
    ) {
        return ResponseEntity.ok(success(
                "한국관광공사 문화유산 장소 전체 동기화 성공",
                spotExternalSyncService.syncAllHeritageSpots(numOfRows)
        ));
    }

    @Operation(
            summary = "미디어콘텐츠 촬영지 페이지 동기화",
            description = "한국문화정보원 미디어콘텐츠 영상 촬영지 데이터의 지정 페이지를 조회해 drama는 K_DRAMA, artist는 K_POP, movie는 K_MOVIE로 저장하거나 변경된 필드만 갱신합니다. 저장 전 TourAPI 검색 결과와 장소명/주소/좌표를 비교해 매칭되는 대표 이미지를 thumbnailUrl에 보강합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "동기화 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 인증키 누락"),
            @ApiResponse(responseCode = "502", description = "외부 API 요청 실패")
    })
    @PostMapping("/media-locations")
    public ResponseEntity<com.Wavey.WaveyService.global.response.ApiResponse<SpotSyncResponse>> syncMediaLocationSpots(
            @Parameter(description = "동기화할 카테고리. 비우면 K_DRAMA, K_POP, K_MOVIE 전체 매핑")
            @RequestParam(required = false) SpotCategory category,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지당 조회 개수") @RequestParam(defaultValue = "100") int perPage
    ) {
        return ResponseEntity.ok(success(
                "미디어콘텐츠 촬영지 페이지 동기화 성공",
                spotExternalSyncService.syncMediaLocationSpots(category, page, perPage)
        ));
    }

    @Operation(
            summary = "미디어콘텐츠 촬영지 전체 동기화",
            description = "한국문화정보원 미디어콘텐츠 영상 촬영지 데이터를 전체 페이지 순회로 동기화합니다. 기존 데이터는 sourceType과 externalContentId 기준으로 비교해 변경된 필드만 갱신하고, 신규 데이터만 추가합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 동기화 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 인증키 누락"),
            @ApiResponse(responseCode = "502", description = "외부 API 요청 실패")
    })
    @PostMapping("/media-locations/all")
    public ResponseEntity<com.Wavey.WaveyService.global.response.ApiResponse<SpotSyncResponse>> syncAllMediaLocationSpots(
            @Parameter(description = "동기화할 카테고리. 비우면 K_DRAMA, K_POP, K_MOVIE 전체 대상")
            @RequestParam(required = false) SpotCategory category,
            @Parameter(description = "페이지당 조회 개수") @RequestParam(defaultValue = "100") int perPage
    ) {
        return ResponseEntity.ok(success(
                "미디어콘텐츠 촬영지 전체 동기화 성공",
                spotExternalSyncService.syncAllMediaLocationSpots(category, perPage)
        ));
    }

    @Operation(
            summary = "촬영지 썸네일 백필",
            description = "이미 저장된 K_DRAMA, K_POP, K_MOVIE 장소 중 thumbnailUrl이 비어 있는 데이터를 TourAPI 검색 결과로 보강합니다. 장소명, 주소, 좌표 기반 점수가 기준 이상인 경우에만 반영합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "백필 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 인증키 누락"),
            @ApiResponse(responseCode = "502", description = "외부 API 요청 실패")
    })
    @PostMapping("/media-locations/thumbnails")
    public ResponseEntity<com.Wavey.WaveyService.global.response.ApiResponse<SpotSyncResponse>> fillMediaLocationThumbnails(
            @Parameter(description = "보강할 카테고리. 비우면 K_DRAMA, K_POP, K_MOVIE 전체 대상")
            @RequestParam(required = false) SpotCategory category,
            @Parameter(description = "한 번에 보강할 최대 장소 수") @RequestParam(defaultValue = "100") int limit
    ) {
        return ResponseEntity.ok(success(
                "촬영지 썸네일 백필 성공",
                spotExternalSyncService.fillMediaLocationThumbnails(category, limit)
        ));
    }
}

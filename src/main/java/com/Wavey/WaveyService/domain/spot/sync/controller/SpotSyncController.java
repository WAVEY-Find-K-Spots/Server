package com.Wavey.WaveyService.domain.spot.sync.controller;

import static com.Wavey.WaveyService.global.response.CommonResponse.success;

import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.sync.dto.SpotPlaceEnrichRequest;
import com.Wavey.WaveyService.domain.spot.sync.dto.SpotSyncResponse;
import com.Wavey.WaveyService.domain.spot.sync.dto.SpotSyncStatusResponse;
import com.Wavey.WaveyService.domain.spot.sync.dto.SpotTitleFeasibilityResponse;
import com.Wavey.WaveyService.domain.spot.sync.dto.SpotTitleReviewRequest;
import com.Wavey.WaveyService.domain.spot.sync.service.SpotSyncService;
import com.Wavey.WaveyService.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/spots/sync")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Spot Sync", description = "외부 TourAPI/미디어 촬영지 데이터를 Spot, Content, SpotContent로 동기화하는 관리자 API")
public class SpotSyncController {

    private final SpotSyncService spotSyncService;

    @PostMapping("/tour/daily")
    @Operation(
            summary = "TourAPI 일일 예산 내 K-HERITAGE 동기화",
            description = "저장된 다음 페이지부터 한 페이지를 처리합니다. 무제한 전체 동기화는 제공하지 않습니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CommonResponse<SpotSyncResponse>> syncTourDaily(
            @Parameter(description = "한 번에 요청할 TourAPI 목록 행 수", example = "3")
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(success("TourAPI 일일 예산 내 문화유산 동기화 성공", spotSyncService.syncTourDaily(size)));
    }

    @PostMapping("/enrich/place")
    @Operation(
            summary = "장소명으로 Spot 필드 최대 보강",
            description = "KorService2 searchKeyword2 결과가 정확히 1건일 때만 detailCommon2, detailIntro2, detailImage2를 호출해 누락 필드를 보강합니다. 복수 후보면 자동 저장하지 않습니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CommonResponse<SpotSyncResponse>> enrichPlace(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "보강할 장소명",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SpotPlaceEnrichRequest.class),
                            examples = @ExampleObject(value = "{\"name\":\"경복궁\"}")))
            @Valid @RequestBody SpotPlaceEnrichRequest request
    ) {
        return ResponseEntity.ok(success("장소명 기반 보강 성공", spotSyncService.enrichPlace(request.getName())));
    }

    @PostMapping("/media/page")
    @Operation(
            summary = "미디어 촬영지 페이지 동기화",
            description = "한 페이지의 미디어 촬영지 데이터를 Content, Spot, SpotContent로 저장합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CommonResponse<SpotSyncResponse>> syncMediaPage(
            @Parameter(description = "선택 카테고리. 비우면 원본 한 페이지 전체를 처리", example = "K_DRAMA")
            @RequestParam(required = false) SpotCategory category,
            @Parameter(description = "외부 API 페이지 번호", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "외부 API 페이지 크기", example = "5")
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(success("미디어 촬영지 페이지 동기화 성공", spotSyncService.syncMediaPage(category, page, size)));
    }

    @GetMapping("/status")
    @Operation(
            summary = "Spot Sync 상태 조회",
            description = "오늘 TourAPI 호출 수, 남은 호출 수, 다음 heritage 페이지 cursor를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CommonResponse<SpotSyncStatusResponse>> status() {
        return ResponseEntity.ok(success("Spot Sync 상태 조회 성공", spotSyncService.status()));
    }

    @PostMapping("/media/by-title")
    @Operation(
            summary = "콘텐츠 제목 기반 SpotContent 자동 생성 가능성 검토",
            description = "현재는 구현하지 않고, 제목 기반 자동 연결의 안전성 검토 결과만 반환합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CommonResponse<SpotTitleFeasibilityResponse>> reviewTitleAutomation(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "검토할 콘텐츠 제목과 카테고리",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SpotTitleReviewRequest.class),
                            examples = @ExampleObject(value = "{\"title\":\"도깨비\",\"category\":\"DRAMA\"}")))
            @Valid @RequestBody SpotTitleReviewRequest request
    ) {
        return ResponseEntity.ok(success("제목 기반 SpotContent 자동 생성 가능성 검토 완료", spotSyncService.reviewTitleAutomation(request)));
    }
}

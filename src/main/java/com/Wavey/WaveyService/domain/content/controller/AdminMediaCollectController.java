package com.Wavey.WaveyService.domain.content.controller;

import com.Wavey.WaveyService.domain.content.dto.MediaCollectResponse;
import com.Wavey.WaveyService.domain.content.dto.MediaVisibilityRequest;
import com.Wavey.WaveyService.domain.content.dto.ContentMediaCollectResponse;
import com.Wavey.WaveyService.domain.content.dto.ContentTrackResponse;
import com.Wavey.WaveyService.domain.content.dto.ContentVideoResponse;
import com.Wavey.WaveyService.domain.content.service.MediaCollectService;
import com.Wavey.WaveyService.domain.content.service.ContentMediaQueryService;
import com.Wavey.WaveyService.domain.content.dto.ContentRequest;
import com.Wavey.WaveyService.domain.content.dto.ContentResponse;
import com.Wavey.WaveyService.domain.content.service.ContentService;
import com.Wavey.WaveyService.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Media Collect", description = "작품 등록 및 유튜브/스포티파이 자동 수집")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminMediaCollectController {

    private final MediaCollectService mediaCollectService;
    private final ContentMediaQueryService workMediaQueryService;
    private final ContentService workService;

    @Operation(summary = "작품 등록")
    @PostMapping("/contents")
    public ResponseEntity<ApiResponse<ContentResponse>> createContent(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = ContentRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "title": "작품 한글 제목 (필수)",
                                      "titleEn": "작품 영문 제목 (선택)",
                                      "category": "ARTIST | DRAMA | MOVIE (필수)"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody ContentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "작품 등록 성공", workService.create(request)));
    }

    @Operation(summary = "유튜브·스포티파이 수집")
    @PostMapping("/contents/{contentId}/collect")
    public ResponseEntity<ApiResponse<ContentMediaCollectResponse>> collect(@PathVariable Long contentId) {
        return ResponseEntity.ok(ApiResponse.success("작품 미디어 수집 성공", mediaCollectService.collectAll(contentId)));
    }

    @Operation(summary = "유튜브만 재수집")
    @PostMapping("/videos/refresh")
    public ResponseEntity<ApiResponse<MediaCollectResponse>> refreshVideos(
            @Parameter(description = "작품 ID") @RequestParam Long contentId
    ) {
        return ResponseEntity.ok(ApiResponse.success("유튜브 수집 성공", mediaCollectService.refreshVideos(contentId)));
    }

    @Operation(summary = "스포티파이만 재수집")
    @PostMapping("/tracks/refresh")
    public ResponseEntity<ApiResponse<MediaCollectResponse>> refreshTracks(
            @Parameter(description = "작품 ID") @RequestParam Long contentId
    ) {
        return ResponseEntity.ok(ApiResponse.success("스포티파이 수집 성공", mediaCollectService.refreshTracks(contentId)));
    }

    @Operation(summary = "영상 숨김")
    @PatchMapping("/videos/{videoId}")
    public ResponseEntity<ApiResponse<ContentVideoResponse>> hideVideo(
            @Parameter(description = "DB 영상 ID") @PathVariable Long videoId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = MediaVisibilityRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "hidden": "true | false (필수). true=숨김, false=다시 공개"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody MediaVisibilityRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "영상 공개 상태 변경 성공",
                workMediaQueryService.updateVideoHidden(videoId, request.getHidden())
        ));
    }

    @Operation(summary = "트랙 숨김")
    @PatchMapping("/tracks/{trackId}")
    public ResponseEntity<ApiResponse<ContentTrackResponse>> hideTrack(
            @Parameter(description = "DB 트랙 ID") @PathVariable Long trackId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = MediaVisibilityRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "hidden": "true | false (필수). true=숨김, false=다시 공개"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody MediaVisibilityRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "트랙 공개 상태 변경 성공",
                workMediaQueryService.updateTrackHidden(trackId, request.getHidden())
        ));
    }
}

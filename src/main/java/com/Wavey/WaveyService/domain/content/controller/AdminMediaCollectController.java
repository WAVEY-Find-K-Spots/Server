package com.Wavey.WaveyService.domain.content.controller;

import com.Wavey.WaveyService.domain.content.dto.MediaCollectResponse;
import com.Wavey.WaveyService.domain.content.dto.MediaVisibilityRequest;
import com.Wavey.WaveyService.domain.content.dto.WorkMediaCollectResponse;
import com.Wavey.WaveyService.domain.content.dto.WorkTrackResponse;
import com.Wavey.WaveyService.domain.content.dto.WorkVideoResponse;
import com.Wavey.WaveyService.domain.content.service.MediaCollectService;
import com.Wavey.WaveyService.domain.content.service.WorkMediaQueryService;
import com.Wavey.WaveyService.domain.work.dto.WorkRequest;
import com.Wavey.WaveyService.domain.work.dto.WorkResponse;
import com.Wavey.WaveyService.domain.work.service.WorkService;
import com.Wavey.WaveyService.global.response.CommonResponse;
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
    private final WorkMediaQueryService workMediaQueryService;
    private final WorkService workService;

    @Operation(summary = "작품 등록")
    @PostMapping("/works")
    public ResponseEntity<CommonResponse<WorkResponse>> createWork(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = WorkRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "title": "작품 한글 제목 (필수)",
                                      "titleEn": "작품 영문 제목 (선택)",
                                      "type": "DRAMA | MOVIE | KPOP (필수)",
                                      "artistName": "가수명. type이 KPOP일 때만 (선택)"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody WorkRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(HttpStatus.CREATED.value(), "작품 등록 성공", workService.create(request)));
    }

    @Operation(summary = "유튜브·스포티파이 수집")
    @PostMapping("/works/{workId}/collect")
    public ResponseEntity<CommonResponse<WorkMediaCollectResponse>> collect(@PathVariable Long workId) {
        return ResponseEntity.ok(CommonResponse.success("작품 미디어 수집 성공", mediaCollectService.collectAll(workId)));
    }

    @Operation(summary = "유튜브만 재수집")
    @PostMapping("/videos/refresh")
    public ResponseEntity<CommonResponse<MediaCollectResponse>> refreshVideos(
            @Parameter(description = "작품 ID") @RequestParam Long workId
    ) {
        return ResponseEntity.ok(CommonResponse.success("유튜브 수집 성공", mediaCollectService.refreshVideos(workId)));
    }

    @Operation(summary = "스포티파이만 재수집")
    @PostMapping("/tracks/refresh")
    public ResponseEntity<CommonResponse<MediaCollectResponse>> refreshTracks(
            @Parameter(description = "작품 ID") @RequestParam Long workId
    ) {
        return ResponseEntity.ok(CommonResponse.success("스포티파이 수집 성공", mediaCollectService.refreshTracks(workId)));
    }

    @Operation(summary = "영상 숨김")
    @PatchMapping("/videos/{videoId}")
    public ResponseEntity<CommonResponse<WorkVideoResponse>> hideVideo(
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
        return ResponseEntity.ok(CommonResponse.success(
                "영상 공개 상태 변경 성공",
                workMediaQueryService.updateVideoHidden(videoId, request.getHidden())
        ));
    }

    @Operation(summary = "트랙 숨김")
    @PatchMapping("/tracks/{trackId}")
    public ResponseEntity<CommonResponse<WorkTrackResponse>> hideTrack(
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
        return ResponseEntity.ok(CommonResponse.success(
                "트랙 공개 상태 변경 성공",
                workMediaQueryService.updateTrackHidden(trackId, request.getHidden())
        ));
    }
}

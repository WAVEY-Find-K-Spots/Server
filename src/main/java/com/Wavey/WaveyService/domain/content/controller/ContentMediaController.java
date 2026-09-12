package com.Wavey.WaveyService.domain.content.controller;

import com.Wavey.WaveyService.domain.content.dto.ContentAlbumResponse;
import com.Wavey.WaveyService.domain.content.dto.ContentMediaCollectResponse;
import com.Wavey.WaveyService.domain.content.dto.ContentTrackResponse;
import com.Wavey.WaveyService.domain.content.dto.ContentVideoResponse;
import com.Wavey.WaveyService.domain.content.dto.MediaCollectResponse;
import com.Wavey.WaveyService.domain.content.dto.MediaVisibilityRequest;
import com.Wavey.WaveyService.domain.content.service.ContentMediaQueryService;
import com.Wavey.WaveyService.domain.content.service.MediaCollectService;
import com.Wavey.WaveyService.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Contents", description = "콘텐츠 CRUD, 영상/앨범/트랙 조회·수집·숨김")
@RestController
@RequiredArgsConstructor
public class ContentMediaController {

    private final ContentMediaQueryService contentMediaQueryService;
    private final MediaCollectService mediaCollectService;

    @Operation(summary = "유튜브 조회")
    @GetMapping("/api/v1/contents/{contentId}/videos")
    public ResponseEntity<ApiResponse<List<ContentVideoResponse>>> videos(@PathVariable Long contentId) {
        return ResponseEntity.ok(ApiResponse.success("유튜브 조회 성공", contentMediaQueryService.listVideos(contentId)));
    }

    @Operation(summary = "앨범 조회")
    @GetMapping("/api/v1/contents/{contentId}/albums")
    public ResponseEntity<ApiResponse<List<ContentAlbumResponse>>> albums(@PathVariable Long contentId) {
        return ResponseEntity.ok(ApiResponse.success("앨범 조회 성공", contentMediaQueryService.listAlbums(contentId)));
    }

    @Operation(summary = "트랙 조회")
    @GetMapping("/api/v1/contents/{contentId}/tracks")
    public ResponseEntity<ApiResponse<List<ContentTrackResponse>>> tracks(@PathVariable Long contentId) {
        return ResponseEntity.ok(ApiResponse.success(
                "트랙 조회 성공",
                contentMediaQueryService.listStandaloneTracks(contentId)
        ));
    }

    @Operation(summary = "앨범 수록 트랙 조회")
    @GetMapping("/api/v1/albums/{contentAlbumId}/tracks")
    public ResponseEntity<ApiResponse<List<ContentTrackResponse>>> albumTracks(@PathVariable Long contentAlbumId) {
        return ResponseEntity.ok(ApiResponse.success(
                "앨범 수록 트랙 조회 성공",
                contentMediaQueryService.listAlbumTracks(contentAlbumId)
        ));
    }

    @Operation(summary = "유튜브·스포티파이 수집")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/v1/contents/{contentId}/collect")
    public ResponseEntity<ApiResponse<ContentMediaCollectResponse>> collect(@PathVariable Long contentId) {
        return ResponseEntity.ok(ApiResponse.success("콘텐츠 미디어 수집 성공", mediaCollectService.collectAll(contentId)));
    }

    @Operation(summary = "유튜브만 재수집")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/v1/contents/{contentId}/videos/refresh")
    public ResponseEntity<ApiResponse<MediaCollectResponse>> refreshVideos(@PathVariable Long contentId) {
        return ResponseEntity.ok(ApiResponse.success("유튜브 재수집 성공", mediaCollectService.refreshVideos(contentId)));
    }

    @Operation(summary = "스포티파이만 재수집")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/v1/contents/{contentId}/tracks/refresh")
    public ResponseEntity<ApiResponse<MediaCollectResponse>> refreshTracks(@PathVariable Long contentId) {
        return ResponseEntity.ok(ApiResponse.success("스포티파이 재수집 성공", mediaCollectService.refreshTracks(contentId)));
    }

    @Operation(summary = "영상 숨김")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/api/v1/videos/{videoId}")
    public ResponseEntity<ApiResponse<ContentVideoResponse>> hideVideo(
            @Parameter(description = "DB 영상 ID") @PathVariable Long videoId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = MediaVisibilityRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "hidden": true
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody MediaVisibilityRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "영상 공개 상태 변경 성공",
                contentMediaQueryService.updateVideoHidden(videoId, request.getHidden())
        ));
    }

    @Operation(summary = "트랙 숨김")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/api/v1/tracks/{trackId}")
    public ResponseEntity<ApiResponse<ContentTrackResponse>> hideTrack(
            @Parameter(description = "DB 트랙 ID") @PathVariable Long trackId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = MediaVisibilityRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "hidden": true
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody MediaVisibilityRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "트랙 공개 상태 변경 성공",
                contentMediaQueryService.updateTrackHidden(trackId, request.getHidden())
        ));
    }

    @Operation(summary = "앨범 숨김")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/api/v1/albums/{albumId}")
    public ResponseEntity<ApiResponse<ContentAlbumResponse>> hideAlbum(
            @Parameter(description = "DB 앨범 ID") @PathVariable Long albumId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = MediaVisibilityRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "hidden": true
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody MediaVisibilityRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "앨범 공개 상태 변경 성공",
                contentMediaQueryService.updateAlbumHidden(albumId, request.getHidden())
        ));
    }
}

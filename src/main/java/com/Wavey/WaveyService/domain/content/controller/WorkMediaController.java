package com.Wavey.WaveyService.domain.content.controller;

import com.Wavey.WaveyService.domain.content.dto.WorkTrackResponse;
import com.Wavey.WaveyService.domain.content.dto.WorkVideoResponse;
import com.Wavey.WaveyService.domain.content.service.WorkMediaQueryService;
import com.Wavey.WaveyService.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Work Media", description = "작품 영상/OST 조회")
@RestController
@RequestMapping("/api/v1/works/{workId}")
@RequiredArgsConstructor
public class WorkMediaController {

    private final WorkMediaQueryService workMediaQueryService;

    @Operation(summary = "작품 영상")
    @GetMapping("/videos")
    public ResponseEntity<ApiResponse<List<WorkVideoResponse>>> videos(@PathVariable Long workId) {
        return ResponseEntity.ok(ApiResponse.success("작품 영상 조회 성공", workMediaQueryService.listVideos(workId)));
    }

    @Operation(summary = "작품 OST")
    @GetMapping("/tracks")
    public ResponseEntity<ApiResponse<List<WorkTrackResponse>>> tracks(@PathVariable Long workId) {
        return ResponseEntity.ok(ApiResponse.success("작품 트랙 조회 성공", workMediaQueryService.listTracks(workId)));
    }
}

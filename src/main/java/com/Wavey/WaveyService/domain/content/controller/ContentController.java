package com.Wavey.WaveyService.domain.content.controller;

import com.Wavey.WaveyService.domain.content.dto.ContentRequest;
import com.Wavey.WaveyService.domain.content.dto.ContentResponse;
import com.Wavey.WaveyService.domain.content.service.ContentService;
import com.Wavey.WaveyService.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Contents", description = "유튜브/스포티파이 콘텐츠 관리 API")
@RestController
@RequestMapping("/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @Operation(summary = "유튜브 콘텐츠 등록")
    @PostMapping("/add/youtube")
    public ResponseEntity<CommonResponse<ContentResponse>> addYoutube(@Valid @RequestBody ContentRequest request) {
        return ResponseEntity.ok(CommonResponse.success("유튜브 콘텐츠 등록 성공", contentService.createYoutube(request)));
    }

    @Operation(summary = "유튜브 콘텐츠 검색")
    @GetMapping("/search/youtube")
    public ResponseEntity<CommonResponse<List<ContentResponse>>> searchYoutube(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(CommonResponse.success("유튜브 콘텐츠 검색 성공", contentService.searchYoutube(keyword)));
    }

    @Operation(summary = "유튜브 콘텐츠 수정")
    @PutMapping("/update/youtube/{contentId}")
    public ResponseEntity<CommonResponse<ContentResponse>> updateYoutube(@PathVariable Long contentId,
                                                                         @Valid @RequestBody ContentRequest request) {
        return ResponseEntity.ok(CommonResponse.success("유튜브 콘텐츠 수정 성공", contentService.updateYoutube(contentId, request)));
    }

    @Operation(summary = "유튜브 콘텐츠 삭제")
    @DeleteMapping("/delete/youtube/{contentId}")
    public ResponseEntity<CommonResponse<Void>> deleteYoutube(@PathVariable Long contentId) {
        contentService.deleteYoutube(contentId);
        return ResponseEntity.ok(CommonResponse.success("유튜브 콘텐츠 삭제 성공", null));
    }

    @Operation(summary = "스포티파이 콘텐츠 등록")
    @PostMapping("/add/spotify")
    public ResponseEntity<CommonResponse<ContentResponse>> addSpotify(@Valid @RequestBody ContentRequest request) {
        return ResponseEntity.ok(CommonResponse.success("스포티파이 콘텐츠 등록 성공", contentService.createSpotify(request)));
    }

    @Operation(summary = "스포티파이 콘텐츠 검색")
    @GetMapping("/search/spotify")
    public ResponseEntity<CommonResponse<List<ContentResponse>>> searchSpotify(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(CommonResponse.success("스포티파이 콘텐츠 검색 성공", contentService.searchSpotify(keyword)));
    }

    @Operation(summary = "스포티파이 콘텐츠 수정")
    @PutMapping("/update/spotify/{contentId}")
    public ResponseEntity<CommonResponse<ContentResponse>> updateSpotify(@PathVariable Long contentId,
                                                                         @Valid @RequestBody ContentRequest request) {
        return ResponseEntity.ok(CommonResponse.success("스포티파이 콘텐츠 수정 성공", contentService.updateSpotify(contentId, request)));
    }

    @Operation(summary = "스포티파이 콘텐츠 삭제")
    @DeleteMapping("/delete/spotify/{contentId}")
    public ResponseEntity<CommonResponse<Void>> deleteSpotify(@PathVariable Long contentId) {
        contentService.deleteSpotify(contentId);
        return ResponseEntity.ok(CommonResponse.success("스포티파이 콘텐츠 삭제 성공", null));
    }
}

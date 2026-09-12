package com.Wavey.WaveyService.domain.content.controller;

import com.Wavey.WaveyService.domain.content.dto.ContentRequest;
import com.Wavey.WaveyService.domain.content.dto.ContentResponse;
import com.Wavey.WaveyService.domain.content.entity.ContentCategory;
import com.Wavey.WaveyService.domain.content.service.ContentService;
import com.Wavey.WaveyService.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

@Tag(name = "Contents", description = "콘텐츠(작품) 등록/조회/수정/삭제")
@RestController
@RequestMapping("/api/v1/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @Operation(summary = "콘텐츠 목록")
    @GetMapping
    public ResponseEntity<CommonResponse<List<ContentResponse>>> list(
            @Parameter(description = "ARTIST | DRAMA | MOVIE") @RequestParam(required = false) ContentCategory category
    ) {
        return ResponseEntity.ok(CommonResponse.success("콘텐츠 목록 조회 성공", contentService.list(category)));
    }

    @Operation(summary = "콘텐츠 단건")
    @GetMapping("/{contentId}")
    public ResponseEntity<CommonResponse<ContentResponse>> get(@PathVariable Long contentId) {
        return ResponseEntity.ok(CommonResponse.success("콘텐츠 조회 성공", contentService.get(contentId)));
    }

    @Operation(summary = "콘텐츠 등록")
    @PostMapping
    public ResponseEntity<CommonResponse<ContentResponse>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = ContentRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "titleKo": "콘텐츠 한글 제목 (필수)",
                                      "titleEn": "콘텐츠 영문 제목 (선택)",
                                      "category": "ARTIST | DRAMA | MOVIE (필수)"
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody ContentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(HttpStatus.CREATED.value(), "콘텐츠 등록 성공", contentService.create(request)));
    }

    @Operation(summary = "콘텐츠 수정")
    @PutMapping("/{contentId}")
    public ResponseEntity<CommonResponse<ContentResponse>> update(
            @PathVariable Long contentId,
            @Valid @RequestBody ContentRequest request
    ) {
        return ResponseEntity.ok(CommonResponse.success("콘텐츠 수정 성공", contentService.update(contentId, request)));
    }

    @Operation(summary = "콘텐츠 삭제")
    @DeleteMapping("/{contentId}")
    public ResponseEntity<CommonResponse<Void>> delete(@PathVariable Long contentId) {
        contentService.delete(contentId);
        return ResponseEntity.ok(CommonResponse.success("콘텐츠 삭제 성공", null));
    }
}

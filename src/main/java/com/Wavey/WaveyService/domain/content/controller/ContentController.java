package com.Wavey.WaveyService.domain.content.controller;

import com.Wavey.WaveyService.domain.content.dto.ContentRequest;
import com.Wavey.WaveyService.domain.content.dto.ContentResponse;
import com.Wavey.WaveyService.domain.content.entity.ContentCategory;
import com.Wavey.WaveyService.domain.content.service.ContentService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Contents", description = "작품 조회/등록")
@RestController
@RequestMapping("/api/v1/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @Operation(summary = "작품 목록")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ContentResponse>>> list(
            @Parameter(description = "ARTIST | DRAMA | MOVIE") @RequestParam(required = false) ContentCategory category
    ) {
        return ResponseEntity.ok(ApiResponse.success("콘텐츠 목록 조회 성공", contentService.list(category)));
    }

    @Operation(summary = "작품 단건")
    @GetMapping("/{contentId}")
    public ResponseEntity<ApiResponse<ContentResponse>> get(@PathVariable Long contentId) {
        return ResponseEntity.ok(ApiResponse.success("콘텐츠 조회 성공", contentService.get(contentId)));
    }

    @Operation(summary = "작품 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<ContentResponse>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(implementation = ContentRequest.class),
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
            @Valid @RequestBody ContentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "콘텐츠 등록 성공", contentService.create(request)));
    }
}

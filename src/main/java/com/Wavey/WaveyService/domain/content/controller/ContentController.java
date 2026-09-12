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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Contents", description = "콘텐츠 CRUD, 영상/앨범/트랙 조회·수집·숨김")
@RestController
@RequestMapping("/api/v1/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @Operation(summary = "콘텐츠 카테고리별 목록")
    @GetMapping
    public ResponseEntity<CommonResponse<List<ContentResponse>>> list(
            @Parameter(description = "ARTIST | DRAMA | MOVIE") @RequestParam(required = false) ContentCategory category
    ) {
        return ResponseEntity.ok(CommonResponse.success("카테고리별 목록 조회 성공", contentService.list(category)));
    }

    @Operation(summary = "콘텐츠 단건 조회")
    @GetMapping("/{contentId}")
    public ResponseEntity<CommonResponse<ContentResponse>> get(@PathVariable Long contentId) {
        return ResponseEntity.ok(CommonResponse.success("콘텐츠 단건 조회 성공", contentService.get(contentId)));
    }

    @Operation(summary = "콘텐츠 등록")
    @PreAuthorize("hasRole('ADMIN')")
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

    @Operation(
            summary = "콘텐츠 수정",
            description = "제목·카테고리 변경 시 YouTube/Spotify 미디어는 자동 재수집하지 않습니다. 필요하면 collect/refresh를 다시 호출하세요."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{contentId}")
    public ResponseEntity<CommonResponse<ContentResponse>> update(
            @PathVariable Long contentId,
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
        return ResponseEntity.ok(CommonResponse.success("콘텐츠 수정 성공", contentService.update(contentId, request)));
    }

    @Operation(
            summary = "콘텐츠 삭제",
            description = "연관 영상·앨범·트랙·스팟 연결(spot_contents)도 함께 삭제합니다."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{contentId}")
    public ResponseEntity<CommonResponse<Void>> delete(@PathVariable Long contentId) {
        contentService.delete(contentId);
        return ResponseEntity.ok(CommonResponse.success("콘텐츠 삭제 성공", null));
    }
}

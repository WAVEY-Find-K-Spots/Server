package com.Wavey.WaveyService.domain.work.controller;

import com.Wavey.WaveyService.domain.work.dto.WorkRequest;
import com.Wavey.WaveyService.domain.work.dto.WorkResponse;
import com.Wavey.WaveyService.domain.work.entity.WorkType;
import com.Wavey.WaveyService.domain.work.service.WorkService;
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

@Tag(name = "Works", description = "작품 조회/등록")
@RestController
@RequestMapping("/api/v1/works")
@RequiredArgsConstructor
public class WorkController {

    private final WorkService workService;

    @Operation(summary = "작품 목록")
    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkResponse>>> list(
            @Parameter(description = "DRAMA | MOVIE | KPOP") @RequestParam(required = false) WorkType type
    ) {
        return ResponseEntity.ok(ApiResponse.success("작품 목록 조회 성공", workService.list(type)));
    }

    @Operation(summary = "작품 단건")
    @GetMapping("/{workId}")
    public ResponseEntity<ApiResponse<WorkResponse>> get(@PathVariable Long workId) {
        return ResponseEntity.ok(ApiResponse.success("작품 조회 성공", workService.get(workId)));
    }

    @Operation(summary = "작품 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<WorkResponse>> create(
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
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "작품 등록 성공", workService.create(request)));
    }
}

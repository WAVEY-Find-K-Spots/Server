package com.Wavey.WaveyService.domain.docent.controller;

import com.Wavey.WaveyService.domain.docent.dto.WebDetectionResponse;
import com.Wavey.WaveyService.domain.docent.service.VisionService;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import com.Wavey.WaveyService.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Vision", description = "AI 이미지 분석 API (Google Cloud Vision)")
@RestController
@RequestMapping("/api/v1/vision")
@RequiredArgsConstructor
public class VisionController {

    private final VisionService visionService;

    @Operation(summary = "번역용 텍스트 추출", description = "이미지 내의 한국어/외국어 텍스트를 추출하여 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "텍스트 추출 성공"),
            @ApiResponse(responseCode = "400", description = "파일이 없거나 비어있음"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다.")
    })
    @PreAuthorize("permitAll()")
    @PostMapping(value = "/extract-text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<String>> extractText(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestPart("file") MultipartFile file
    ) {
        validateFile(file);
        String result = visionService.extractText(file.getResource());
        return ResponseEntity.ok(CommonResponse.success("텍스트 추출 성공", result));
    }

    @Operation(summary = "문화재 명칭 인식", description = "이미지 속 문화재나 랜드마크의 이름을 인식합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "문화재 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다.")
    })
    @PreAuthorize("permitAll()")
    @PostMapping(value = "/heritage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<List<String>>> getHeritageInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "사용자 현재 위도 (선택)") @RequestParam(value = "lat", required = false) Double lat,
            @Parameter(description = "사용자 현재 경도 (선택)") @RequestParam(value = "lng", required = false) Double lng
    ) {
        validateFile(file);
        List<String> result = visionService.detectHeritage(file.getResource(), lat, lng);
        return ResponseEntity.ok(CommonResponse.success("문화재 정보 조회 성공", result));
    }

    @Operation(summary = "물건/장소 웹 검색 정보", description = "이미지 분석을 통해 관련 웹 검색 키워드 및 사물 정보를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "웹 검색 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다.")
    })
    @PreAuthorize("permitAll()")
    @PostMapping(value = "/web-search", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<WebDetectionResponse>> getWebSearch(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @RequestPart("file") MultipartFile file
    ) {
        validateFile(file);
        return ResponseEntity.ok(CommonResponse.success("웹 검색 정보 조회 성공", visionService.searchWebInfo(file.getResource())));
    }

    // 파일 유효성 검증 공통 메서드
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.COMMON_FILE_EMPTY);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(ErrorCode.COMMON_INVALID_FILE_TYPE);
        }

        long maxBytes = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxBytes) {
            throw new CustomException(ErrorCode.COMMON_FILE_SIZE_EXCEEDED);
        }
    }
}
package com.Wavey.WaveyService.domain.docent.controller;

import com.Wavey.WaveyService.domain.docent.dto.VisionAnalysisResponse;
import com.Wavey.WaveyService.domain.docent.dto.VisionFeature;
import com.Wavey.WaveyService.domain.docent.service.VisionRequestRateLimiter;
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
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

@Tag(name = "Vision", description = "Google Cloud Vision 기반 이미지 분석 API")
@RestController
@RequestMapping("/api/v1/vision")
@RequiredArgsConstructor
public class VisionController {

    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> SUPPORTED_IMAGE_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp"
    );

    private final VisionService visionService;
    private final VisionRequestRateLimiter requestRateLimiter;

    @Operation(
            summary = "선택 기능 기반 이미지 분석",
            description = """
                    이미지와 실행할 기능 목록을 전달합니다.
                    features에는 TRANSLATION, HERITAGE, WEB_SEARCH를 지정할 수 있습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이미지 분석 성공"),
            @ApiResponse(responseCode = "400", description = "파일 또는 기능 목록이 유효하지 않음"),
            @ApiResponse(responseCode = "401", description = "토큰이 유효하지 않거나 만료됨"),
            @ApiResponse(responseCode = "429", description = "Google Vision API 할당량 초과"),
            @ApiResponse(responseCode = "500", description = "이미지 분석 실패")
    })
    @PreAuthorize("permitAll()")
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<VisionAnalysisResponse>> analyze(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(hidden = true) HttpServletRequest request,
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "실행 기능 목록: TRANSLATION, HERITAGE, WEB_SEARCH")
            @RequestParam("features") Set<VisionFeature> features,
            @Parameter(description = "사용자 현재 위도 (문화재 분석 시 선택)")
            @RequestParam(value = "lat", required = false) Double lat,
            @Parameter(description = "사용자 현재 경도 (문화재 분석 시 선택)")
            @RequestParam(value = "lng", required = false) Double lng
    ) {
        validateFile(file);
        validateFeatures(features);
        requestRateLimiter.check(user, request == null ? null : request.getRemoteAddr());

        VisionAnalysisResponse result = visionService.analyze(
                file.getResource(),
                features,
                lat,
                lng
        );
        return ResponseEntity.ok(CommonResponse.success("이미지 분석 성공", result));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.COMMON_FILE_EMPTY);
        }

        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new CustomException(ErrorCode.COMMON_FILE_SIZE_EXCEEDED);
        }

        String declaredType = normalizeContentType(file.getContentType());
        if (declaredType == null || !SUPPORTED_IMAGE_TYPES.contains(declaredType)) {
            throw new CustomException(ErrorCode.VISION_IMAGE_TYPE_UNSUPPORTED);
        }

        String decodedType = detectImageType(file);
        if (!declaredType.equals(decodedType)) {
            throw new CustomException(ErrorCode.VISION_IMAGE_TYPE_UNSUPPORTED);
        }
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return null;
        }
        try {
            MediaType mediaType = MediaType.parseMediaType(contentType);
            return (mediaType.getType() + "/" + mediaType.getSubtype()).toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String detectImageType(MultipartFile file) {
        try (ImageInputStream input = ImageIO.createImageInputStream(
                new ByteArrayInputStream(file.getBytes())
        )) {
            if (input == null) {
                throw new CustomException(ErrorCode.VISION_IMAGE_TYPE_UNSUPPORTED);
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            while (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    String detectedType = imageMediaType(reader.getFormatName());
                    if (detectedType == null) {
                        continue;
                    }
                    reader.setInput(input, true, true);
                    if (reader.read(0) != null) {
                        return detectedType;
                    }
                } finally {
                    reader.dispose();
                }
            }
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof CustomException customException) {
                throw customException;
            }
            throw new CustomException(ErrorCode.VISION_IMAGE_TYPE_UNSUPPORTED);
        }
        throw new CustomException(ErrorCode.VISION_IMAGE_TYPE_UNSUPPORTED);
    }

    private String imageMediaType(String formatName) {
        return switch (formatName.toUpperCase(Locale.ROOT)) {
            case "JPEG", "JPG" -> MediaType.IMAGE_JPEG_VALUE;
            case "PNG" -> MediaType.IMAGE_PNG_VALUE;
            case "WEBP" -> "image/webp";
            default -> null;
        };
    }

    private void validateFeatures(Set<VisionFeature> features) {
        if (features == null || features.isEmpty()) {
            throw new CustomException(ErrorCode.COMMON_INVALID_PARAMETER);
        }
    }
}

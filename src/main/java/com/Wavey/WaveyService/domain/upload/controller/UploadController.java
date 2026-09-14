package com.Wavey.WaveyService.domain.upload.controller;

import com.Wavey.WaveyService.domain.upload.dto.PresignedUploadRequest;
import com.Wavey.WaveyService.domain.upload.dto.PresignedUploadResponse;
import com.Wavey.WaveyService.domain.upload.service.UploadService;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Upload", description = "파일 업로드 공통 API")
@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @Operation(summary = "업로드 URL 발급", description = "S3 호환 스토리지에 직접 업로드할 수 있는 Presigned URL을 발급합니다.")
    @PostMapping("/presigned-url")
    public ResponseEntity<CommonResponse<PresignedUploadResponse>> createPresignedUrl(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PresignedUploadRequest request
    ) {
        return ResponseEntity.ok(CommonResponse.success(
                "업로드 URL 발급 성공",
                uploadService.createUploadUrl(request.category(), user.getId(), request.contentType())
        ));
    }
}

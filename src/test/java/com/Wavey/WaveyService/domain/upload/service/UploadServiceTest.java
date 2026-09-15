package com.Wavey.WaveyService.domain.upload.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.Wavey.WaveyService.domain.upload.dto.PresignedUploadResponse;
import com.Wavey.WaveyService.domain.upload.enums.UploadCategory;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@ExtendWith(MockitoExtension.class)
class UploadServiceTest {

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private PresignedPutObjectRequest presignedPutObjectRequest;

    @Mock
    private PresignedGetObjectRequest presignedGetObjectRequest;

    private UploadService uploadService;

    @BeforeEach
    void setUp() {
        uploadService = new UploadService(s3Presigner);
        ReflectionTestUtils.setField(uploadService, "bucket", "test-bucket");
        ReflectionTestUtils.setField(uploadService, "publicBaseUrl", "https://test.storageapi.dev/test-bucket");
        ReflectionTestUtils.setField(uploadService, "presignedUrlTtlSeconds", 600L);
        ReflectionTestUtils.setField(uploadService, "presignedGetTtlSeconds", 3600L);
    }

    @Test
    void 허용된_이미지_타입이면_업로드_URL을_발급한다() throws Exception {
        when(s3Presigner.presignPutObject(org.mockito.ArgumentMatchers.<java.util.function.Consumer<software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest.Builder>>any()))
                .thenReturn(presignedPutObjectRequest);
        when(presignedPutObjectRequest.url()).thenReturn(new URL("https://test.storageapi.dev/test-bucket/profile/1/abc.jpg?signed=1"));

        PresignedUploadResponse response = uploadService.createUploadUrl(UploadCategory.PROFILE, 1L, "image/jpeg");

        assertThat(response.uploadUrl()).contains("signed=1");
        assertThat(response.fileUrl()).startsWith("https://test.storageapi.dev/test-bucket/profile/1/");
        assertThat(response.fileUrl()).endsWith(".jpg");
    }

    @Test
    void 허용되지_않은_타입이면_예외가_발생한다() {
        assertThatThrownBy(() -> uploadService.createUploadUrl(UploadCategory.PROFILE, 1L, "application/pdf"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMON_INVALID_FILE_TYPE);
    }

    @Test
    void 본인_소유_경로가_아니면_예외가_발생한다() {
        assertThatThrownBy(() -> uploadService.validateOwnedUrl(UploadCategory.PROFILE, 1L,
                "https://test.storageapi.dev/test-bucket/profile/2/abc.jpg"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UPLOAD_INVALID_FILE_URL);
    }

    @Test
    void 본인_소유_경로면_통과한다() {
        uploadService.validateOwnedUrl(UploadCategory.PROFILE, 1L,
                "https://test.storageapi.dev/test-bucket/profile/1/abc.jpg");
    }

    @Test
    void 버킷_소속_URL이면_presigned_GET으로_바꿔서_반환한다() throws Exception {
        when(s3Presigner.presignGetObject(org.mockito.ArgumentMatchers.<java.util.function.Consumer<software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest.Builder>>any()))
                .thenReturn(presignedGetObjectRequest);
        when(presignedGetObjectRequest.url())
                .thenReturn(new URL("https://test.storageapi.dev/test-bucket/profile/1/abc.jpg?signed=get"));

        String result = uploadService.resolveAccessUrl("https://test.storageapi.dev/test-bucket/profile/1/abc.jpg");

        assertThat(result).contains("signed=get");
    }

    @Test
    void 버킷_소속이_아닌_URL은_그대로_반환한다() {
        String external = "https://cdn.example.com/some-image.png";
        assertThat(uploadService.resolveAccessUrl(external)).isEqualTo(external);
    }

    @Test
    void null_URL도_그대로_반환한다() {
        assertThat(uploadService.resolveAccessUrl(null)).isNull();
    }
}

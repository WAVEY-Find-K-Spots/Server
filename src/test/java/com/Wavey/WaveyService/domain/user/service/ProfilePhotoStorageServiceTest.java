package com.Wavey.WaveyService.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.Wavey.WaveyService.domain.user.dto.PhotoUploadUrlResponse;
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
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@ExtendWith(MockitoExtension.class)
class ProfilePhotoStorageServiceTest {

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private PresignedPutObjectRequest presignedPutObjectRequest;

    private ProfilePhotoStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new ProfilePhotoStorageService(s3Presigner);
        ReflectionTestUtils.setField(storageService, "bucket", "test-bucket");
        ReflectionTestUtils.setField(storageService, "publicBaseUrl", "https://test.storageapi.dev/test-bucket");
        ReflectionTestUtils.setField(storageService, "presignedUrlTtlSeconds", 600L);
    }

    @Test
    void 허용된_이미지_타입이면_업로드_URL을_발급한다() throws Exception {
        when(s3Presigner.presignPutObject(org.mockito.ArgumentMatchers.<java.util.function.Consumer<software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest.Builder>>any()))
                .thenReturn(presignedPutObjectRequest);
        when(presignedPutObjectRequest.url()).thenReturn(new URL("https://test.storageapi.dev/test-bucket/profile/1/abc.jpg?signed=1"));

        PhotoUploadUrlResponse response = storageService.createUploadUrl(1L, "image/jpeg");

        assertThat(response.uploadUrl()).contains("signed=1");
        assertThat(response.photoUrl()).startsWith("https://test.storageapi.dev/test-bucket/profile/1/");
        assertThat(response.photoUrl()).endsWith(".jpg");
    }

    @Test
    void 허용되지_않은_타입이면_예외가_발생한다() {
        assertThatThrownBy(() -> storageService.createUploadUrl(1L, "application/pdf"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMON_INVALID_FILE_TYPE);
    }

    @Test
    void 본인_소유_경로가_아니면_예외가_발생한다() {
        assertThatThrownBy(() -> storageService.validateOwnedPhotoUrl(1L,
                "https://test.storageapi.dev/test-bucket/profile/2/abc.jpg"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_INVALID_PHOTO_URL);
    }

    @Test
    void 본인_소유_경로면_통과한다() {
        storageService.validateOwnedPhotoUrl(1L,
                "https://test.storageapi.dev/test-bucket/profile/1/abc.jpg");
    }
}

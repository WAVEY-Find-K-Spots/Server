package com.Wavey.WaveyService.domain.upload.service;

import com.Wavey.WaveyService.domain.upload.dto.PresignedUploadResponse;
import com.Wavey.WaveyService.domain.upload.enums.UploadCategory;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final S3Presigner s3Presigner;

    @Value("${storage.bucket}")
    private String bucket;

    @Value("${storage.public-base-url}")
    private String publicBaseUrl;

    @Value("${storage.presigned-url-ttl-seconds}")
    private long presignedUrlTtlSeconds;

    @Value("${storage.presigned-get-ttl-seconds}")
    private long presignedGetTtlSeconds;

    public PresignedUploadResponse createUploadUrl(UploadCategory category, Long ownerId, String contentType) {
        String extension = category.extensionFor(contentType);
        if (extension == null) {
            throw new CustomException(ErrorCode.COMMON_INVALID_FILE_TYPE);
        }

        String key = "%s/%d/%s.%s".formatted(category.keyPrefix(), ownerId, UUID.randomUUID(), extension);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(builder -> builder
                .signatureDuration(Duration.ofSeconds(presignedUrlTtlSeconds))
                .putObjectRequest(putObjectRequest));

        String fileUrl = publicBaseUrl + "/" + key;

        return new PresignedUploadResponse(
                presigned.url().toString(),
                fileUrl,
                Instant.now().plusSeconds(presignedUrlTtlSeconds)
        );
    }

    public void validateOwnedUrl(UploadCategory category, Long ownerId, String fileUrl) {
        String expectedPrefix = publicBaseUrl + "/" + category.keyPrefix() + "/" + ownerId + "/";
        if (!fileUrl.startsWith(expectedPrefix)) {
            throw new CustomException(ErrorCode.UPLOAD_INVALID_FILE_URL);
        }
    }

    /**
     * 저장된 URL이 이 버킷(publicBaseUrl)에 속한 객체면, 매번 새로 서명한 presigned GET URL로 바꿔서 반환한다.
     * 버킷은 private이라 고정된 공개 URL로는 접근할 수 없기 때문이다.
     * 우리 버킷 소속이 아닌 URL(외부 CDN 등)은 그대로 반환한다. {@code null}도 그대로 반환.
     */
    public String resolveAccessUrl(String storedUrl) {
        if (storedUrl == null || !storedUrl.startsWith(publicBaseUrl + "/")) {
            return storedUrl;
        }
        String key = storedUrl.substring(publicBaseUrl.length() + 1);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(builder -> builder
                .signatureDuration(Duration.ofSeconds(presignedGetTtlSeconds))
                .getObjectRequest(getObjectRequest));

        return presigned.url().toString();
    }
}

package com.Wavey.WaveyService.domain.user.service;

import com.Wavey.WaveyService.domain.user.dto.PhotoUploadUrlResponse;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Service
@RequiredArgsConstructor
public class ProfilePhotoStorageService {

    private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final S3Presigner s3Presigner;

    @Value("${storage.bucket}")
    private String bucket;

    @Value("${storage.public-base-url}")
    private String publicBaseUrl;

    @Value("${storage.presigned-url-ttl-seconds}")
    private long presignedUrlTtlSeconds;

    public PhotoUploadUrlResponse createUploadUrl(Long userId, String contentType) {
        String extension = ALLOWED_CONTENT_TYPES.get(contentType);
        if (extension == null) {
            throw new CustomException(ErrorCode.COMMON_INVALID_FILE_TYPE);
        }

        String key = "profile/%d/%s.%s".formatted(userId, UUID.randomUUID(), extension);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(builder -> builder
                .signatureDuration(Duration.ofSeconds(presignedUrlTtlSeconds))
                .putObjectRequest(putObjectRequest));

        String photoUrl = publicBaseUrl + "/" + key;

        return new PhotoUploadUrlResponse(
                presigned.url().toString(),
                photoUrl,
                Instant.now().plusSeconds(presignedUrlTtlSeconds)
        );
    }

    public void validateOwnedPhotoUrl(Long userId, String photoUrl) {
        String expectedPrefix = publicBaseUrl + "/profile/" + userId + "/";
        if (!photoUrl.startsWith(expectedPrefix)) {
            throw new CustomException(ErrorCode.USER_INVALID_PHOTO_URL);
        }
    }
}

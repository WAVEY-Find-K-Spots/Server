package com.Wavey.WaveyService.domain.docent.client;

import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import com.google.api.gax.rpc.ApiException;
import com.google.api.gax.rpc.StatusCode;
import com.google.cloud.spring.vision.CloudVisionTemplate;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleVisionClientTest {

    @Mock
    private CloudVisionTemplate cloudVisionTemplate;
    @Mock
    private ImageAnnotatorClient imageAnnotatorClient;

    private GoogleVisionClient client;
    private ApiException quotaException;

    @BeforeEach
    void setUp() {
        client = new GoogleVisionClient(cloudVisionTemplate, imageAnnotatorClient);
        quotaException = mock(ApiException.class);
        StatusCode statusCode = mock(StatusCode.class);
        when(statusCode.getCode()).thenReturn(StatusCode.Code.RESOURCE_EXHAUSTED);
        when(quotaException.getStatusCode()).thenReturn(statusCode);
    }

    @Test
    void 랜드마크_할당량_초과는_429_오류로_매핑한다() {
        when(imageAnnotatorClient.batchAnnotateImages(anyList())).thenThrow(quotaException);

        CustomException exception = catchThrowableOfType(
                () -> client.detectLandmarks(new ByteArrayResource(new byte[]{1}), null, null),
                CustomException.class
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VISION_TOKEN_EXHAUSTED);
    }

    @Test
    void 웹_검색_할당량_초과는_429_오류로_매핑한다() {
        when(cloudVisionTemplate.analyzeImage(
                any(Resource.class),
                eq(Feature.Type.WEB_DETECTION)
        )).thenThrow(quotaException);

        CustomException exception = catchThrowableOfType(
                () -> client.detectWeb(new ByteArrayResource(new byte[]{1})),
                CustomException.class
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VISION_TOKEN_EXHAUSTED);
    }
}

package com.Wavey.WaveyService.domain.docent.client;

import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import com.Wavey.WaveyService.domain.docent.dto.OcrTextData;
import com.google.api.gax.rpc.ApiException;
import com.google.api.gax.rpc.StatusCode;
import com.google.cloud.spring.vision.CloudVisionTemplate;
import com.google.cloud.vision.v1.*;
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

    @BeforeEach
    void setUp() {
        client = new GoogleVisionClient(cloudVisionTemplate, imageAnnotatorClient);
    }

    @Test
    void 랜드마크_할당량_초과는_429_오류로_매핑한다() {
        ApiException quotaException = quotaException();
        when(imageAnnotatorClient.batchAnnotateImages(anyList())).thenThrow(quotaException);

        CustomException exception = catchThrowableOfType(
                () -> client.detectLandmarks(new ByteArrayResource(new byte[]{1}), null, null),
                CustomException.class
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VISION_TOKEN_EXHAUSTED);
    }

    @Test
    void 웹_검색_할당량_초과는_429_오류로_매핑한다() {
        ApiException quotaException = quotaException();
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

    @Test
    void OCR_문단의_텍스트_좌표와_신뢰도를_반환한다() {
        BoundingPoly polygon = BoundingPoly.newBuilder()
                .addVertices(Vertex.newBuilder().setX(10).setY(20))
                .addVertices(Vertex.newBuilder().setX(110).setY(20))
                .addVertices(Vertex.newBuilder().setX(110).setY(50))
                .addVertices(Vertex.newBuilder().setX(10).setY(50))
                .build();
        Paragraph paragraph = Paragraph.newBuilder()
                .addWords(Word.newBuilder()
                        .addSymbols(Symbol.newBuilder().setText("김치찌개")))
                .setBoundingBox(polygon)
                .setConfidence(0.97f)
                .build();
        TextAnnotation annotation = TextAnnotation.newBuilder()
                .setText("김치찌개\n")
                .addPages(Page.newBuilder()
                        .addBlocks(Block.newBuilder().addParagraphs(paragraph)))
                .build();
        when(cloudVisionTemplate.analyzeImage(
                any(Resource.class),
                eq(Feature.Type.TEXT_DETECTION)
        )).thenReturn(AnnotateImageResponse.newBuilder()
                .setFullTextAnnotation(annotation)
                .build());

        OcrTextData result = client.extractText(new ByteArrayResource(new byte[]{1}));

        assertThat(result.text()).isEqualTo("김치찌개\n");
        assertThat(result.blocks()).hasSize(1);
        assertThat(result.blocks().getFirst().text()).isEqualTo("김치찌개");
        assertThat(result.blocks().getFirst().confidence()).isEqualTo(0.97f);
        assertThat(result.blocks().getFirst().polygon())
                .extracting(point -> point.x() + "," + point.y())
                .containsExactly("10,20", "110,20", "110,50", "10,50");
    }

    private ApiException quotaException() {
        ApiException quotaException = mock(ApiException.class);
        StatusCode statusCode = mock(StatusCode.class);
        when(statusCode.getCode()).thenReturn(StatusCode.Code.RESOURCE_EXHAUSTED);
        when(quotaException.getStatusCode()).thenReturn(statusCode);
        return quotaException;
    }
}

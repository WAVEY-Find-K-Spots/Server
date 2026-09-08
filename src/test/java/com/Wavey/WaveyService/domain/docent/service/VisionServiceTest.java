package com.Wavey.WaveyService.domain.docent.service;

import com.Wavey.WaveyService.domain.docent.client.GoogleVisionClient;
import com.Wavey.WaveyService.domain.docent.dto.TranslationResponse;
import com.Wavey.WaveyService.domain.docent.dto.VisionAnalysisResponse;
import com.Wavey.WaveyService.domain.docent.dto.VisionFeature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisionServiceTest {

    @Mock
    private GoogleVisionClient googleVisionClient;
    @Mock
    private TranslationService translationService;
    @Mock
    private HeritageService heritageService;
    @Mock
    private WebSearchService webSearchService;
    @Mock
    private Resource imageResource;

    @InjectMocks
    private VisionService visionService;

    @Test
    void 요청한_기능만_실행한다() {
        when(googleVisionClient.extractText(imageResource)).thenReturn("육회");
        TranslationResponse translation = new TranslationResponse("육회", "Yukhoe", List.of());
        when(translationService.process("육회")).thenReturn(translation);

        VisionAnalysisResponse response = visionService.analyze(
                imageResource,
                Set.of(VisionFeature.TRANSLATION),
                null,
                null
        );

        assertThat(response.getTranslation()).isSameAs(translation);
        assertThat(response.getHeritage()).isNull();
        assertThat(response.getWebSearch()).isNull();
        verify(googleVisionClient, never()).detectLandmarks(imageResource, null, null);
        verify(googleVisionClient, never()).detectWeb(imageResource);
    }

    @Test
    void 번역과_국가유산을_함께_요청해도_OCR은_한번만_실행한다() {
        when(googleVisionClient.extractText(imageResource)).thenReturn("서울 숭례문");
        when(googleVisionClient.detectLandmarks(imageResource, 37.5599, 126.9753))
                .thenReturn(List.of("Sungnyemun Gate"));
        when(heritageService.process(
                List.of("Sungnyemun Gate"), "서울 숭례문", 37.5599, 126.9753
        )).thenReturn(List.of());

        visionService.analyze(
                imageResource,
                Set.of(VisionFeature.TRANSLATION, VisionFeature.HERITAGE),
                37.5599,
                126.9753
        );

        verify(googleVisionClient, times(1)).extractText(imageResource);
        verify(translationService).process("서울 숭례문");
        verify(heritageService).process(
                List.of("Sungnyemun Gate"), "서울 숭례문", 37.5599, 126.9753
        );
    }
}
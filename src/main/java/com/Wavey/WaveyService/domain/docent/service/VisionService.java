package com.Wavey.WaveyService.domain.docent.service;

import com.Wavey.WaveyService.domain.docent.client.GoogleVisionClient;
import com.Wavey.WaveyService.domain.docent.dto.HeritageResponse;
import com.Wavey.WaveyService.domain.docent.dto.TranslationResponse;
import com.Wavey.WaveyService.domain.docent.dto.VisionAnalysisResponse;
import com.Wavey.WaveyService.domain.docent.dto.VisionFeature;
import com.Wavey.WaveyService.domain.docent.dto.WebDetectionRawData;
import com.Wavey.WaveyService.domain.docent.dto.WebDetectionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VisionService {

    private final GoogleVisionClient googleVisionClient;
    private final TranslationService translationService;
    private final HeritageService heritageService;
    private final WebSearchService webSearchService;

    public VisionAnalysisResponse analyze(
            Resource imageResource, Set<VisionFeature> features, Double latitude, Double longitude
    ) {
        TranslationResponse translation = null;
        List<HeritageResponse> heritage = null;
        WebDetectionResponse webSearch = null;

        String rawText = null;
        if (features.contains(VisionFeature.TRANSLATION) || features.contains(VisionFeature.HERITAGE)) {
            rawText = googleVisionClient.extractText(imageResource);
        }
        if (features.contains(VisionFeature.TRANSLATION)) {
            translation = translationService.process(rawText);
        }
        if (features.contains(VisionFeature.HERITAGE)) {
            List<String> rawCandidates = googleVisionClient.detectLandmarks(
                    imageResource, latitude, longitude
            );
            heritage = heritageService.process(rawCandidates, rawText, latitude, longitude);
        }
        if (features.contains(VisionFeature.WEB_SEARCH)) {
            WebDetectionRawData rawWebData = googleVisionClient.detectWeb(imageResource);
            webSearch = webSearchService.process(rawWebData);
        }
        return new VisionAnalysisResponse(translation, heritage, webSearch);
    }
}

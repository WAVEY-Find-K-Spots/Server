package com.Wavey.WaveyService.domain.docent.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class VisionAnalysisResponse {
    private TranslationResponse translation;
    private List<HeritageResponse> heritage;
    private WebDetectionResponse webSearch;
}

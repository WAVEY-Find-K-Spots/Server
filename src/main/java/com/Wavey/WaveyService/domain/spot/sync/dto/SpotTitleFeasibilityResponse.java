package com.Wavey.WaveyService.domain.spot.sync.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SpotTitleFeasibilityResponse {
    private boolean implementationRecommended;
    private String status;
    private String reason;
}

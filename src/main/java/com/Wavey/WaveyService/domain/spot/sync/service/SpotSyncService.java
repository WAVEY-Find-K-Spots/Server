package com.Wavey.WaveyService.domain.spot.sync.service;

import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.sync.dto.SpotSyncResponse;
import com.Wavey.WaveyService.domain.spot.sync.dto.SpotSyncStatusResponse;
import com.Wavey.WaveyService.domain.spot.sync.dto.SpotTitleFeasibilityResponse;
import com.Wavey.WaveyService.domain.spot.sync.dto.SpotTitleReviewRequest;

public interface SpotSyncService {
    SpotSyncResponse syncTourDaily(int size);
    SpotSyncResponse enrichPlace(String name);
    SpotSyncResponse syncMediaPage(SpotCategory category, int page, int size);
    SpotSyncStatusResponse status();
    SpotTitleFeasibilityResponse reviewTitleAutomation(SpotTitleReviewRequest request);
}

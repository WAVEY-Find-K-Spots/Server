package com.Wavey.WaveyService.domain.spot.service;

import com.Wavey.WaveyService.domain.spot.dto.request.SpotCreateRequest;
import com.Wavey.WaveyService.domain.spot.dto.request.SpotUpdateRequest;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotResponse;

public interface SpotService {

    SpotResponse createSpot(
            SpotCreateRequest request
    );

    SpotResponse getSpot(
            Long spotId,
            Long userId
    );

    SpotResponse updateSpot(
            Long spotId,
            SpotUpdateRequest request
    );

    void deleteSpot(Long spotId);
}
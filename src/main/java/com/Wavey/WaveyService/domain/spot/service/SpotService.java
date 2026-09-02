package com.Wavey.WaveyService.domain.spot.service;

import com.Wavey.WaveyService.domain.spot.dto.request.SpotCreateRequest;
import com.Wavey.WaveyService.domain.spot.dto.request.SpotUpdateRequest;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotListResponse;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotResponse;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import java.math.BigDecimal;
import java.util.List;

public interface SpotService {

    SpotResponse createSpot(SpotCreateRequest request);

    SpotResponse getSpot(Long spotId);

    List<SpotListResponse> getSpots(SpotCategory category, Long regionId);

    List<SpotListResponse> getSpotsByMapBounds(BigDecimal minLat, BigDecimal maxLat, BigDecimal minLng, BigDecimal maxLng);

    SpotResponse updateSpot(Long spotId, SpotUpdateRequest request);

    void deleteSpot(Long spotId);
}

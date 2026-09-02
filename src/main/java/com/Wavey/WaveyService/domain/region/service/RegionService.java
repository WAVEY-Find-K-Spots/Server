package com.Wavey.WaveyService.domain.region.service;

import com.Wavey.WaveyService.domain.region.dto.request.RegionCreateRequest;
import com.Wavey.WaveyService.domain.region.dto.request.RegionUpdateRequest;
import com.Wavey.WaveyService.domain.region.dto.response.RegionResponse;
import java.util.List;

public interface RegionService {

    RegionResponse createRegion(RegionCreateRequest request);

    RegionResponse getRegion(Long regionId);

    List<RegionResponse> getRegions();

    RegionResponse updateRegion(Long regionId, RegionUpdateRequest request);

    void deleteRegion(Long regionId);
}

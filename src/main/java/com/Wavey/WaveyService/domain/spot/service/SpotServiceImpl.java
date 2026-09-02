package com.Wavey.WaveyService.domain.spot.service;

import com.Wavey.WaveyService.domain.region.repository.RegionRepository;
import com.Wavey.WaveyService.domain.spot.converter.SpotConverter;
import com.Wavey.WaveyService.domain.spot.dto.request.SpotCreateRequest;
import com.Wavey.WaveyService.domain.spot.dto.request.SpotUpdateRequest;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotListResponse;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotResponse;
import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.enums.SpotSourceType;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class SpotServiceImpl implements SpotService {

    private final SpotRepository spotRepository;
    private final RegionRepository regionRepository;

    @Override
    public SpotResponse createSpot(SpotCreateRequest request) {
        validateRegion(request.getRegionId());
        Spot spot = SpotConverter.toEntity(request);

        if (spot.getExternalContentId() != null
                && existsExternalSpot(request.getSourceType(), spot.getExternalContentId())) {
            throw new CustomException(ErrorCode.SPOT_EXTERNAL_DATA_ALREADY_EXISTS);
        }

        Spot savedSpot = spotRepository.save(spot);
        return SpotConverter.toResponse(savedSpot);
    }

    @Override
    @Transactional(readOnly = true)
    public SpotResponse getSpot(Long spotId) {
        Spot spot = findSpotById(spotId);
        return SpotConverter.toResponse(spot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpotListResponse> getSpots(SpotCategory category, Long regionId) {
        return spotRepository.search(category, regionId)
                .stream()
                .map(SpotConverter::toListResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpotListResponse> getSpotsByMapBounds(BigDecimal minLat, BigDecimal maxLat, BigDecimal minLng, BigDecimal maxLng) {
        validateMapBounds(minLat, maxLat, minLng, maxLng);
        return spotRepository.findMapBounds(minLat, maxLat, minLng, maxLng)
                .stream()
                .map(SpotConverter::toListResponse)
                .toList();
    }

    @Override
    public SpotResponse updateSpot(Long spotId, SpotUpdateRequest request) {
        Spot spot = findSpotById(spotId);
        SpotConverter.updateEntity(spot, request);
        return SpotConverter.toResponse(spot);
    }

    @Override
    public void deleteSpot(Long spotId) {
        Spot spot = findSpotById(spotId);
        spotRepository.delete(spot);
    }

    private boolean existsExternalSpot(SpotSourceType sourceType, String externalContentId) {
        if (!StringUtils.hasText(externalContentId)) {
            return false;
        }
        return spotRepository.existsExternal(sourceType, externalContentId.trim());
    }

    private Spot findSpotById(Long spotId) {
        return spotRepository.findById(spotId)
                .orElseThrow(() -> new CustomException(ErrorCode.SPOT_NOT_FOUND));
    }

    private void validateMapBounds(BigDecimal minLat, BigDecimal maxLat, BigDecimal minLng, BigDecimal maxLng) {
        if (minLat.compareTo(maxLat) > 0 || minLng.compareTo(maxLng) > 0) {
            throw new CustomException(ErrorCode.SPOT_INVALID_MAP_BOUNDS);
        }
    }

    private void validateRegion(Long regionId) {
        if (!regionRepository.existsById(regionId)) {
            throw new CustomException(ErrorCode.REGION_NOT_FOUND);
        }
    }
}

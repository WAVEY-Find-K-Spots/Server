package com.Wavey.WaveyService.domain.spot.service;

import com.Wavey.WaveyService.domain.spot.dto.response.SpotSaveResponse;
import com.Wavey.WaveyService.domain.spot.entity.SavedSpot;
import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.repository.SavedSpotRepository;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpotSaveService {

    private final SavedSpotRepository savedSpotRepository;
    private final SpotRepository spotRepository;

    @Transactional
    public SpotSaveResponse save(Long spotId, Long userId) {
        Spot spot = findSpot(spotId);
        if (!savedSpotRepository.existsByUserIdAndSpotId(userId, spotId)) {
            savedSpotRepository.save(SavedSpot.builder().userId(userId).spotId(spotId).build());
            spot.changeSavedCount(1);
        }
        return new SpotSaveResponse(spotId, true, spot.getSavedCount());
    }

    @Transactional
    public SpotSaveResponse unsave(Long spotId, Long userId) {
        Spot spot = findSpot(spotId);
        if (savedSpotRepository.existsByUserIdAndSpotId(userId, spotId)) {
            savedSpotRepository.deleteByUserIdAndSpotId(userId, spotId);
            spot.changeSavedCount(-1);
        }
        return new SpotSaveResponse(spotId, false, spot.getSavedCount());
    }

    private Spot findSpot(Long spotId) {
        return spotRepository.findById(spotId)
                .orElseThrow(() -> new CustomException(ErrorCode.SPOT_NOT_FOUND));
    }
}

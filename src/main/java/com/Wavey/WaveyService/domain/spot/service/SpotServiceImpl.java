package com.Wavey.WaveyService.domain.spot.service;

import com.Wavey.WaveyService.domain.region.repository.RegionRepository;
import com.Wavey.WaveyService.domain.spot.converter.SpotConverter;
import com.Wavey.WaveyService.domain.spot.dto.request.SpotCreateRequest;
import com.Wavey.WaveyService.domain.spot.dto.request.SpotUpdateRequest;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotResponse;
import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpotServiceImpl implements SpotService {

    private final SpotRepository spotRepository;
    private final RegionRepository regionRepository;
    private final SpotConverter spotConverter;

    @Override
    @Transactional
    public SpotResponse createSpot(
            SpotCreateRequest request
    ) {
        validateRegion(request.regionId());

        Spot spot =
                spotRepository.save(
                        spotConverter.toEntity(request)
                );

        return spotConverter.toResponse(
                spot,
                false,
                currentLocale()
        );
    }

    @Override
    public SpotResponse getSpot(
            Long spotId,
            Long userId
    ) {
        Spot spot = findSpot(spotId);

        return spotConverter.toResponse(
                spot,
                false,
                currentLocale()
        );
    }

    @Override
    @Transactional
    public SpotResponse updateSpot(
            Long spotId,
            SpotUpdateRequest request
    ) {
        Spot spot = findSpot(spotId);

        if (request.regionId() != null) {
            validateRegion(request.regionId());
        }

        spotConverter.updateEntity(
                spot,
                request
        );

        return spotConverter.toResponse(
                spot,
                false,
                currentLocale()
        );
    }

    @Override
    @Transactional
    public void deleteSpot(Long spotId) {
        spotRepository.delete(
                findSpot(spotId)
        );
    }

    private Spot findSpot(Long spotId) {
        return spotRepository.findById(spotId)
                .orElseThrow(
                        () ->
                                new CustomException(
                                        ErrorCode.SPOT_NOT_FOUND
                                )
                );
    }

    private void validateRegion(Long regionId) {
        if (!regionRepository.existsById(regionId)) {
            throw new CustomException(
                    ErrorCode.REGION_NOT_FOUND
            );
        }
    }

    private Locale currentLocale() {
        return LocaleContextHolder.getLocale();
    }
}
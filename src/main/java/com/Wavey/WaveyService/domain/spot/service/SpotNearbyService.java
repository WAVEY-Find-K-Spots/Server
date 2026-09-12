package com.Wavey.WaveyService.domain.spot.service;

import com.Wavey.WaveyService.domain.spot.converter.SpotConverter;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotNearbyResponse;
import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.domain.spot.support.SpotGeoSupport;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpotNearbyService {

    private static final int NEARBY_SIZE = 4;

    private final SpotRepository spotRepository;
    private final SpotConverter spotConverter;

    public List<SpotNearbyResponse> findNearby(
            Long spotId,
            double radiusMeters
    ) {
        Spot baseSpot =
                spotRepository.findById(spotId)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                ErrorCode.SPOT_NOT_FOUND
                                        )
                        );

        SpotGeoSupport.Bounds bounds =
                SpotGeoSupport.bounds(
                        baseSpot.getLatitude(),
                        baseSpot.getLongitude(),
                        radiusMeters
                );

        List<Spot> spots =
                spotRepository.findNearby(
                        spotId,
                        baseSpot.getLatitude()
                                .doubleValue(),
                        baseSpot.getLongitude()
                                .doubleValue(),
                        bounds.minLat(),
                        bounds.maxLat(),
                        bounds.minLng(),
                        bounds.maxLng(),
                        radiusMeters,
                        NEARBY_SIZE
                );

        Locale locale =
                LocaleContextHolder.getLocale();

        return spots.stream()
                .map(
                        spot ->
                                spotConverter.toNearbyResponse(
                                        spot,
                                        SpotGeoSupport.meters(
                                                baseSpot.getLatitude(),
                                                baseSpot.getLongitude(),
                                                spot
                                        ),
                                        locale
                                )
                )
                .toList();
    }
}
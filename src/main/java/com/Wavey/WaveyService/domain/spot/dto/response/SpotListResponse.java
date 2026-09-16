package com.Wavey.WaveyService.domain.spot.dto.response;

import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;

public record SpotListResponse(
        Long spotId,
        String name,
        String description,
        SpotCategory category,
        String imageUrl,
        String imageAttribution,
        Double avgRating,
        long reviewCount,
        boolean saved,
        Double distanceMeters
) {}
package com.Wavey.WaveyService.domain.spot.dto.response;

public record SpotNearbyResponse(
        Long spotId,
        String name,
        String description,
        String address,
        String imageUrl,
        String imageAttribution,
        Double avgRating,
        Double distanceMeters
) {}
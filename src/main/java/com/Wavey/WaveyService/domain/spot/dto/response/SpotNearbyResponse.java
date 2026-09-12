package com.Wavey.WaveyService.domain.spot.dto.response;

public record SpotNearbyResponse(
        Long spotId,
        String name,
        String description,
        String address,
        String imageUrl,
        Double avgRating,
        Double distanceMeters
) {}
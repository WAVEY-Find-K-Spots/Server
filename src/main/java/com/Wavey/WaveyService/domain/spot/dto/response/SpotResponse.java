package com.Wavey.WaveyService.domain.spot.dto.response;

import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;

import java.math.BigDecimal;

public record SpotResponse(
        Long spotId,
        String name,
        String description,
        SpotCategory category,
        String imageUrl,
        Double avgRating,
        long reviewCount,
        boolean saved,
        String openingHours,
        String breakTime,
        String closedDays,
        String address,
        String transportInfo,
        String tel,
        BigDecimal latitude,
        BigDecimal longitude
) {}
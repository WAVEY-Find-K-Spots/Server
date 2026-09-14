package com.Wavey.WaveyService.domain.review.dto.response;

import com.Wavey.WaveyService.domain.user.enums.CountryCode;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long reviewId,
        Long spotId,
        Long userId,
        String authorName,
        String authorImageUrl,
        CountryCode countryCode,
        String countryName,
        Double rating,
        String body,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

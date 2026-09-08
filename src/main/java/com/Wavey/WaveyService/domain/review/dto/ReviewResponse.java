package com.Wavey.WaveyService.domain.review.dto;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long reviewId,
        Long spotId,
        String spotName,
        String spotImageUrl,
        Long userId,
        String authorName,
        String authorImageUrl,
        String countryCode,
        int rating,
        String body,
        String language,
        LocalDateTime createdAt) {}

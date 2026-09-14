package com.Wavey.WaveyService.domain.review.repository;

public interface ReviewRatingStats {

    Double getAverageRating();

    Long getReviewCount();
}
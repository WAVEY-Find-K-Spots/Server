package com.Wavey.WaveyService.domain.review.service;

import com.Wavey.WaveyService.domain.review.dto.request.ReviewCreateRequest;
import com.Wavey.WaveyService.domain.review.dto.request.ReviewUpdateRequest;

import com.Wavey.WaveyService.domain.review.dto.response.MyReviewListResponse;
import com.Wavey.WaveyService.domain.review.dto.response.ReviewListResponse;
import com.Wavey.WaveyService.domain.review.dto.response.ReviewResponse;

public interface ReviewService {

    ReviewResponse create(Long spotId, Long userId, ReviewCreateRequest request);

    ReviewListResponse getReviews(Long spotId, int page, int size);

    ReviewResponse getReview(Long reviewId);

    MyReviewListResponse getMyReviews(Long userId, int page, int size);

    ReviewResponse update(Long reviewId, Long userId, ReviewUpdateRequest request);

    void delete(Long reviewId, Long userId);
}
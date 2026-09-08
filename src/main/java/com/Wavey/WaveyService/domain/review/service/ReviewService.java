package com.Wavey.WaveyService.domain.review.service;

import com.Wavey.WaveyService.domain.review.dto.*;
import com.Wavey.WaveyService.domain.review.entity.*;
import com.Wavey.WaveyService.domain.review.repository.*;
import com.Wavey.WaveyService.domain.spot.repository.*;
import com.Wavey.WaveyService.domain.spot.service.*;
import com.Wavey.WaveyService.domain.user.repository.*;
import com.Wavey.WaveyService.global.exception.*;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {
    private final ReviewRepository reviews;
    private final SpotRepository spots;
    private final UserRepository users;
    private final UserSettingsRepository settings;
    private final SpotDiscoveryService discovery;

    @Transactional
    public ReviewResponse create(Long spotId, Long userId, ReviewRequest request) {
        var spot =
                spots.findLockedById(spotId)
                        .orElseThrow(() -> new CustomException(ErrorCode.SPOT_NOT_FOUND));
        var review =
                reviews.saveAndFlush(
                        Review.builder()
                                .spotId(spotId)
                                .userId(userId)
                                .rating(request.rating())
                                .body(request.body().trim())
                                .countryCode(request.countryCode())
                                .language(discovery.language(userId, request.language()))
                                .build());
        spot.updateRating(reviews.average(spotId), reviews.countBySpotId(spotId));
        return response(review);
    }

    public Page<ReviewResponse> list(Long spotId, int page, int size) {
        discovery.require(spotId);
        return reviews.findBySpotId(
                        spotId,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id")))
                .map(this::response);
    }

    public Page<ReviewResponse> mine(Long userId, int page, int size) {
        return reviews.findByUserId(
                        userId,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id")))
                .map(this::response);
    }

    private ReviewResponse response(Review r) {
        var user = users.findById(r.getUserId());
        var spot = spots.findById(r.getSpotId());
        var profile = settings.findByUserId(r.getUserId());
        return new ReviewResponse(
                r.getId(),
                r.getSpotId(),
                spot.map(s -> s.getName()).orElse(null),
                spot.map(s -> s.getThumbnailUrl()).orElse(null),
                r.getUserId(),
                user.map(u -> u.getName()).orElse("탈퇴한 사용자"),
                profile.map(s -> s.getProfileImageUrl()).orElse(null),
                r.getCountryCode(),
                r.getRating(),
                r.getBody(),
                r.getLanguage(),
                r.getCreatedAt());
    }
}

package com.Wavey.WaveyService.domain.review.converter;

import com.Wavey.WaveyService.domain.review.dto.request.ReviewCreateRequest;
import com.Wavey.WaveyService.domain.review.dto.response.ReviewResponse;
import com.Wavey.WaveyService.domain.review.entity.Review;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.domain.user.entity.UserSetting;
import com.Wavey.WaveyService.domain.user.enums.CountryCode;

import org.springframework.stereotype.Component;

@Component
public class ReviewConverter {

    public Review toEntity(Long spotId, Long userId, ReviewCreateRequest request) {
        return Review.builder()
                .spotId(spotId)
                .userId(userId)
                .rating(request.rating())
                .body(request.body())
                .build();
    }

    public ReviewResponse toResponse(Review review, User user, UserSetting settings) {
        CountryCode countryCode = user != null ? user.getCountryCode() : null;
        String language = settings != null && settings.getLanguage() != null ? settings.getLanguage() : "ko";

        String countryName = null;

        if (countryCode != null) {
            countryName = "en".equalsIgnoreCase(language)
                    ? countryCode.getNameEn()
                    : countryCode.getNameKo();
        }

        return new ReviewResponse(
                review.getId(),
                review.getSpotId(),
                review.getUserId(),
                user != null ? user.getName() : "탈퇴한 사용자",
                settings != null ? settings.getProfileImageUrl() : null,
                countryCode,
                countryName,
                review.getRating(),
                review.getBody(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
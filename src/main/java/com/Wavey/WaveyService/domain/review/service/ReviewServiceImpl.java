package com.Wavey.WaveyService.domain.review.service;

import com.Wavey.WaveyService.domain.notification.event.NotificationEvent;
import com.Wavey.WaveyService.domain.review.dto.request.ReviewCreateRequest;
import com.Wavey.WaveyService.domain.review.dto.request.ReviewUpdateRequest;
import com.Wavey.WaveyService.domain.review.dto.response.MyReviewListResponse;
import com.Wavey.WaveyService.domain.review.dto.response.ReviewListResponse;
import com.Wavey.WaveyService.domain.review.dto.response.ReviewResponse;
import com.Wavey.WaveyService.domain.review.entity.Review;
import com.Wavey.WaveyService.domain.review.repository.ReviewRatingStats;
import com.Wavey.WaveyService.domain.review.repository.ReviewRepository;
import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.domain.user.entity.UserSetting;
import com.Wavey.WaveyService.domain.user.enums.CountryCode;
import com.Wavey.WaveyService.domain.user.repository.UserRepository;
import com.Wavey.WaveyService.domain.user.repository.UserSettingsRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private static final Sort REVIEW_SORT = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

    private final ReviewRepository reviewRepository;
    private final SpotRepository spotRepository;
    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ReviewResponse create(Long spotId, Long userId, ReviewCreateRequest request) {
        Spot spot = getLockedSpot(spotId);
        User user = getUser(userId);

        if (reviewRepository.existsBySpotIdAndUserId(spotId, userId)) {
            throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.builder()
                .userId(userId)
                .spotId(spotId)
                .rating(request.rating())
                .body(request.body().strip())
                .build();

        reviewRepository.saveAndFlush(review);
        updateSpotRating(spot);
        eventPublisher.publishEvent(
                new NotificationEvent.ReviewCreated(
                        userId,
                        review.getId(),
                        spotId,
                        spot.getNameKo(),
                        spot.getNameEn()));

        return buildSingleResponse(review, user, spot);
    }

    @Override
    public ReviewListResponse getReviews(Long spotId, int page, int size) {
        Spot spot = getSpot(spotId);

        Slice<Review> reviewSlice = reviewRepository.findAllBySpotId(spotId, PageRequest.of(page, size, REVIEW_SORT));
        List<Review> reviews = reviewSlice.getContent();

        Map<Long, User> userMap = loadUsers(reviews);
        Map<Long, UserSetting> settingsMap = loadUserSettings(reviews);

        List<ReviewResponse> responses = reviews.stream()
                .map(review -> toResponse(review, userMap.get(review.getUserId()), settingsMap.get(review.getUserId()), spot))
                .toList();

        ReviewRatingStats stats = reviewRepository.findRatingStats(spotId);

        return new ReviewListResponse(
                roundRating(stats.getAverageRating()),
                valueOrZero(stats.getReviewCount()),
                responses,
                reviewSlice.getNumber(),
                reviewSlice.getSize(),
                reviewSlice.hasNext()
        );
    }

    @Override
    public ReviewResponse getReview(Long reviewId) {
        Review review = getReviewEntity(reviewId);
        return buildSingleResponse(review);
    }

    @Override
    public MyReviewListResponse getMyReviews(Long userId, int page, int size) {
        User user = getUser(userId);

        Slice<Review> reviewSlice = reviewRepository.findAllByUserId(userId, PageRequest.of(page, size, REVIEW_SORT));
        List<Review> reviews = reviewSlice.getContent();

        UserSetting settings = userSettingsRepository.findByUserId(userId).orElse(null);

        Set<Long> spotIds = reviews.stream()
                .map(Review::getSpotId)
                .collect(Collectors.toSet());

        Map<Long, Spot> spotMap = spotIds.isEmpty()
                ? Collections.emptyMap()
                : spotRepository.findAllById(spotIds).stream().collect(Collectors.toMap(Spot::getId, Function.identity()));

        List<ReviewResponse> responses = reviews.stream()
                .map(review -> toResponse(review, user, settings, spotMap.get(review.getSpotId())))
                .toList();

        return new MyReviewListResponse(responses, reviewSlice.getNumber(), reviewSlice.getSize(), reviewSlice.hasNext());
    }

    @Override
    @Transactional
    public ReviewResponse update(Long reviewId, Long userId, ReviewUpdateRequest request) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        Spot spot = getLockedSpot(review.getSpotId());

        review.update(request.rating(), request.body());

        reviewRepository.flush();
        updateSpotRating(spot);

        User user = getUser(userId);

        return buildSingleResponse(review, user, spot);
    }

    @Override
    @Transactional
    public void delete(Long reviewId, Long userId) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        Spot spot = getLockedSpot(review.getSpotId());

        reviewRepository.delete(review);
        reviewRepository.flush();

        updateSpotRating(spot);
    }

    private Review getReviewEntity(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Spot getSpot(Long spotId) {
        return spotRepository.findById(spotId)
                .orElseThrow(() -> new CustomException(ErrorCode.SPOT_NOT_FOUND));
    }

    private Spot getLockedSpot(Long spotId) {
        return spotRepository.findLockedById(spotId)
                .orElseThrow(() -> new CustomException(ErrorCode.SPOT_NOT_FOUND));
    }

    private void updateSpotRating(Spot spot) {
        ReviewRatingStats stats = reviewRepository.findRatingStats(spot.getId());

        double averageRating = stats.getAverageRating() == null ? 0.0 : stats.getAverageRating();
        long reviewCount = valueOrZero(stats.getReviewCount());

        spot.updateRating(averageRating, reviewCount);
    }

    private ReviewResponse buildSingleResponse(Review review) {
        User user = userRepository.findById(review.getUserId()).orElse(null);
        Spot spot = spotRepository.findById(review.getSpotId()).orElse(null);

        return buildSingleResponse(review, user, spot);
    }

    private ReviewResponse buildSingleResponse(Review review, User user, Spot spot) {
        UserSetting settings = userSettingsRepository.findByUserId(review.getUserId()).orElse(null);
        return toResponse(review, user, settings, spot);
    }

    private Map<Long, User> loadUsers(Collection<Review> reviews) {
        if (reviews.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> userIds = reviews.stream()
                .map(Review::getUserId)
                .collect(Collectors.toSet());

        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private Map<Long, UserSetting> loadUserSettings(Collection<Review> reviews) {
        if (reviews.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> userIds = reviews.stream()
                .map(Review::getUserId)
                .collect(Collectors.toSet());

        List<UserSetting> settings = userSettingsRepository.findAllByUserIdIn(userIds);
        Map<Long, UserSetting> result = new HashMap<>();

        for (UserSetting setting : settings) {
            result.put(setting.getUserId(), setting);
        }

        return result;
    }

    private ReviewResponse toResponse(Review review, User user, UserSetting settings, Spot spot) {
        CountryCode countryCode = user != null ? user.getCountryCode() : null;

        // String language = settings != null && settings.getLanguage() != null ? settings.getLanguage() : "ko";

        return new ReviewResponse(
                review.getId(),
                review.getSpotId(),
                review.getUserId(),
                user != null ? user.getName() : "탈퇴한 사용자",
                settings != null ? settings.getProfileImageUrl() : null,
                countryCode,

                // countryCode != null ? countryCode.getDisplayName(language) : null,
                null,

                review.getRating(),
                review.getBody(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }

    private String getSpotName(Spot spot, String language) {
        if (spot == null) {
            return null;
        }

        if ("en".equalsIgnoreCase(language) && spot.getNameEn() != null && !spot.getNameEn().isBlank()) {
            return spot.getNameEn();
        }

        return spot.getNameKo();
    }

    private double roundRating(Double rating) {
        if (rating == null) {
            return 0.0;
        }

        return BigDecimal.valueOf(rating).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }
}

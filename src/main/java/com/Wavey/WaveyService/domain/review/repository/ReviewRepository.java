package com.Wavey.WaveyService.domain.review.repository;

import com.Wavey.WaveyService.domain.review.entity.Review;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Slice<Review> findAllBySpotId(Long spotId, Pageable pageable);

    Slice<Review> findAllByUserId(Long userId, Pageable pageable);

    Optional<Review> findByIdAndUserId(Long reviewId, Long userId);

    boolean existsBySpotIdAndUserId(Long spotId, Long userId);

    @Query("""
            SELECT
                AVG(r.rating) AS averageRating,
                COUNT(r) AS reviewCount
            FROM Review r
            WHERE r.spotId = :spotId
            """)
    ReviewRatingStats findRatingStats(@Param("spotId") Long spotId);
}
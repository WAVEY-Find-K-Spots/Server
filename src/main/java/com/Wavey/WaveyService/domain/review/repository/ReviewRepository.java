package com.Wavey.WaveyService.domain.review.repository;

import com.Wavey.WaveyService.domain.review.entity.Review;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findBySpotId(Long spotId, Pageable pageable);

    Page<Review> findByUserId(Long userId, Pageable pageable);

    long countBySpotId(Long spotId);

    @Query("select avg(r.rating) from Review r where r.spotId = :spotId")
    Double average(Long spotId);
}

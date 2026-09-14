package com.Wavey.WaveyService.domain.stamp.repository;

import com.Wavey.WaveyService.domain.stamp.entity.UserStamp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserStampRepository extends JpaRepository<UserStamp, Long> {

    Optional<UserStamp> findByUserIdAndStampId(Long userId, Long stampId);

    Optional<UserStamp> findByUserIdAndSpotId(Long userId, Long spotId);

    List<UserStamp> findByUserId(Long userId);

    long countByUserId(Long userId);

    void deleteBySpotId(Long spotId);
}

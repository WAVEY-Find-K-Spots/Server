package com.Wavey.WaveyService.domain.user.repository;

import com.Wavey.WaveyService.domain.user.entity.SavedSpot;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface SavedSpotRepository extends JpaRepository<SavedSpot, Long> {
    boolean existsByUserIdAndSpotId(Long userId, Long spotId);

    void deleteByUserIdAndSpotId(Long userId, Long spotId);

    long countByUserId(Long userId);

    Page<SavedSpot> findByUserId(Long userId, Pageable pageable);

    List<SavedSpot> findByUserIdAndSpotIdIn(Long userId, Collection<Long> spotIds);
}

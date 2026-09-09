package com.Wavey.WaveyService.domain.stamp.repository;

import com.Wavey.WaveyService.domain.stamp.entity.UserBadge;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    List<UserBadge> findByUserId(Long userId);

    long countByUserId(Long userId);

    boolean existsByUserIdAndBadgeId(Long userId, Long badgeId);
}

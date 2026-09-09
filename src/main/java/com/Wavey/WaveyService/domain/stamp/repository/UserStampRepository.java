package com.Wavey.WaveyService.domain.stamp.repository;

import com.Wavey.WaveyService.domain.stamp.entity.UserStamp;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface UserStampRepository extends JpaRepository<UserStamp, Long> {

    Optional<UserStamp> findByUserIdAndStampId(Long userId, Long stampId);

    List<UserStamp> findByUserId(Long userId);

    long countByUserId(Long userId);
}

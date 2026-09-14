package com.Wavey.WaveyService.domain.user.repository;

import com.Wavey.WaveyService.domain.user.entity.UserSetting;

import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface UserSettingsRepository extends JpaRepository<UserSetting, Long> {
    Optional<UserSetting> findByUserId(Long userId);
    List<UserSetting> findAllByUserIdIn(Collection<Long> userIds);
}

package com.Wavey.WaveyService.domain.user.repository;

import com.Wavey.WaveyService.domain.user.entity.UserSettings;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
    Optional<UserSettings> findByUserId(Long userId);
}

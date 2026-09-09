package com.Wavey.WaveyService.domain.notification.repository;

import com.Wavey.WaveyService.domain.notification.entity.Notification;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.time.LocalDateTime;
import java.util.*;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    long countByUserIdAndReadAtIsNull(Long userId);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query(
            "update Notification n set n.readAt = :now where n.userId = :userId and n.readAt is"
                + " null")
    int readAll(Long userId, LocalDateTime now);
}

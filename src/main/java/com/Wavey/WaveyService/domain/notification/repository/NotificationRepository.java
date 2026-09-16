package com.Wavey.WaveyService.domain.notification.repository;

import com.Wavey.WaveyService.domain.notification.entity.Notification;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndReadAtIsNull(Long userId);

    boolean existsByUserIdAndEventKey(Long userId, String eventKey);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            "update Notification n set n.readAt = :readAt "
                    + "where n.userId = :userId and n.readAt is null")
    int markAllRead(
            @Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);
}

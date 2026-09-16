package com.Wavey.WaveyService.domain.notification.entity;

import com.Wavey.WaveyService.domain.notification.converter.NotificationTypeConverter;
import com.Wavey.WaveyService.domain.notification.enums.NotificationTargetType;
import com.Wavey.WaveyService.domain.notification.enums.NotificationType;
import com.Wavey.WaveyService.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "notifications",
        indexes = {
            @Index(name = "idx_notifications_user_created", columnList = "user_id, created_at"),
            @Index(name = "idx_notifications_user_read", columnList = "user_id, read_at")
        },
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_notifications_user_event",
                        columnNames = {"user_id", "event_key"}))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Convert(converter = NotificationTypeConverter.class)
    @Column(nullable = false, length = 20)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 150)
    private String titleKo;

    @Column(name = "title_en", length = 150)
    private String titleEn;

    @Column(name = "body", nullable = false, length = 500)
    private String bodyKo;

    @Column(name = "body_en", length = 500)
    private String bodyEn;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 20)
    private NotificationTargetType targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "event_key", length = 160)
    private String eventKey;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    public void markRead(LocalDateTime now) {
        if (readAt == null) {
            readAt = now;
        }
    }
}

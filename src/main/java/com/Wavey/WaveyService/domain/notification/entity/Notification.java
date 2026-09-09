package com.Wavey.WaveyService.domain.notification.entity;

import com.Wavey.WaveyService.global.common.BaseEntity;

import jakarta.persistence.*;

import lombok.*;

import java.time.*;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String body;

    private String titleEn;

    @Column(length = 2000)
    private String bodyEn;

    private String targetType;
    private Long targetId;
    private LocalDateTime readAt;
}

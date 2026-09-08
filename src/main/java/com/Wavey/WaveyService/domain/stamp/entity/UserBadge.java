package com.Wavey.WaveyService.domain.stamp.entity;

import com.Wavey.WaveyService.global.common.BaseEntity;

import jakarta.persistence.*;

import lombok.*;

import java.time.*;

@Entity
@Table(
        name = "user_badges",
        uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "badgeId"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBadge extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long badgeId;

    @Column(nullable = false)
    private LocalDateTime acquiredAt;
}

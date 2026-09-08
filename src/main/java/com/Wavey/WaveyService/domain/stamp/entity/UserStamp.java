package com.Wavey.WaveyService.domain.stamp.entity;

import com.Wavey.WaveyService.global.common.BaseEntity;

import jakarta.persistence.*;

import lombok.*;

import java.time.*;

@Entity
@Table(
        name = "user_stamps",
        uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "stampId"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStamp extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long stampId;

    @Column(nullable = false)
    private Long spotId;

    @Column(nullable = false)
    private Long regionId;

    @Column(nullable = false)
    private LocalDateTime acquiredAt;
}

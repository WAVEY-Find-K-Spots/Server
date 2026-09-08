package com.Wavey.WaveyService.domain.user.entity;

import com.Wavey.WaveyService.global.common.BaseEntity;

import jakarta.persistence.*;

import lombok.*;

import java.time.*;

@Entity
@Table(
        name = "saved_spots",
        uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "spotId"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedSpot extends BaseEntity {
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long spotId;
}

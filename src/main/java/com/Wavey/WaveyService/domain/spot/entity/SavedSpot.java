package com.Wavey.WaveyService.domain.spot.entity;

import com.Wavey.WaveyService.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "saved_spots",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_saved_spots_user_spot",
                columnNames = {"user_id", "spot_id"}
        ),
        indexes = {
                @Index(name = "idx_saved_spots_user_created", columnList = "user_id, created_at"),
                @Index(name = "idx_saved_spots_spot", columnList = "spot_id")
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedSpot extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "spot_id", nullable = false)
    private Long spotId;
}

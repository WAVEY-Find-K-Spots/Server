package com.Wavey.WaveyService.domain.stamp.entity;

import com.Wavey.WaveyService.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "badge_spots",
        uniqueConstraints = @UniqueConstraint(columnNames = {"badgeId", "spotId"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeSpot extends BaseEntity {

    @Column(nullable = false)
    private Long badgeId;

    @Column(nullable = false)
    private Long spotId;
}

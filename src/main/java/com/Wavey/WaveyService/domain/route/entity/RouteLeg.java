package com.Wavey.WaveyService.domain.route.entity;

import com.Wavey.WaveyService.global.common.BaseEntity;

import jakarta.persistence.*;

import lombok.*;

import java.time.*;

@Entity
@Table(
        name = "route_legs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"fromSpotId", "toSpotId", "mode"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteLeg extends BaseEntity {

    @Column(nullable = false)
    private Long fromSpotId;

    @Column(nullable = false)
    private Long toSpotId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TravelMode mode;

    @Column(nullable = false)
    private int distanceMeters;

    @Column(nullable = false)
    private int durationSeconds;

    @Column(columnDefinition = "TEXT")
    private String encodedPolyline;

    private String instruction;
    private String instructionEn;

    public enum TravelMode {
        WALKING,
        TRANSIT,
        DRIVING
    }
}

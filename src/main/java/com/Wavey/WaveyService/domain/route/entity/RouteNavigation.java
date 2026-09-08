package com.Wavey.WaveyService.domain.route.entity;

import com.Wavey.WaveyService.global.common.BaseEntity;

import jakarta.persistence.*;

import lombok.*;

import java.time.*;

@Entity
@Table(name = "route_navigation")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteNavigation extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Long routeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RouteLeg.TravelMode mode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String spotIds;

    private int currentIndex;
    private boolean completed;
    @Version
    private Long version;
}

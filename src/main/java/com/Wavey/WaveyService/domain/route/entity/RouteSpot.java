package com.Wavey.WaveyService.domain.route.entity;

import com.Wavey.WaveyService.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "route_spots")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteSpot extends BaseTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;

    @Column(name = "spot_id", nullable = false)
    private Long spotId;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    public void updateSequence(int newOrder) {
        this.sequenceOrder = newOrder;
    }
}

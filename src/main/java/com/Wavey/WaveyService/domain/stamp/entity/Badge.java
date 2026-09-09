package com.Wavey.WaveyService.domain.stamp.entity;

import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.global.common.BaseEntity;

import jakarta.persistence.*;

import lombok.*;

import java.time.*;

@Entity
@Table(name = "badges")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Badge extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String nameEn;
    private String description;
    private String descriptionEn;

    @Column(length = 1000)
    private String imageUrl;

    @Column(nullable = false)
    private int requiredStamps;

    private Long regionId;

    @Enumerated(EnumType.STRING)
    private SpotCategory category;
}

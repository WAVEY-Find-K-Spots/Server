package com.Wavey.WaveyService.domain.stamp.entity;

import com.Wavey.WaveyService.global.common.BaseEntity;

import jakarta.persistence.*;

import lombok.*;

import java.time.*;

@Entity
@Table(name = "stamps")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stamp extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Long spotId;

    @Column(nullable = false)
    private String name;

    private String nameEn;

    @Column(length = 1000)
    private String imageUrl;

    private String description;
    private String descriptionEn;
    @Builder.Default
    private int radiusMeters = 150;
}

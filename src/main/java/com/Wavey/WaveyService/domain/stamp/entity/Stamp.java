package com.Wavey.WaveyService.domain.stamp.entity;

import com.Wavey.WaveyService.global.common.BaseEntity;

import jakarta.persistence.*;

import lombok.*;

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

    /** 획득 가능 반경(m). 현재는 전체 150 고정. */
    @Builder.Default
    private int radiusMeters = 150;
}

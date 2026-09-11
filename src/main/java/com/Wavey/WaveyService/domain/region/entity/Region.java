package com.Wavey.WaveyService.domain.region.entity;

import com.Wavey.WaveyService.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Region extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String nameKo;

    @Column(nullable = false, length = 50)
    private String nameEn;

    public Long getRegionId() {
        return getId();
    }

    public void update(String nameKo, String nameEn) {
        this.nameKo = nameKo;
        this.nameEn = nameEn;
    }
}

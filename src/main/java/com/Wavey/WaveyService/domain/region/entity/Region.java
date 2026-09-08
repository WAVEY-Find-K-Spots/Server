package com.Wavey.WaveyService.domain.region.entity;

import com.Wavey.WaveyService.global.common.BaseEntity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
        name = "regions",
        indexes = {
            @Index(name = "idx_regions_code", columnList = "code", unique = true),
            @Index(name = "idx_regions_name", columnList = "name")
        })
@AttributeOverride(name = "id", column = @Column(name = "region_id"))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Region extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    private String nameEn;

    public Long getRegionId() {
        return getId();
    }

    public void update(String name, String code, BigDecimal latitude, BigDecimal longitude) {
        this.name = name;
        this.code = code;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}

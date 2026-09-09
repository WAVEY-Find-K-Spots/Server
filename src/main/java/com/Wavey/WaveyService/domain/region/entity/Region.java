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


@Entity
@Table(
        name = "regions",
        indexes = {
            @Index(name = "idx_regions_code", columnList = "code", unique = true),
            @Index(name = "idx_regions_name_ko", columnList = "name_ko")
        })
@AttributeOverride(name = "id", column = @Column(name = "region_id"))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Region extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name_ko", nullable = false, length = 50)
    private String nameKo;

    @Column(name = "name_en", length = 50)
    private String nameEn;

    public Long getRegionId() {
        return getId();
    }

    public void update(String name, String code, java.math.BigDecimal latitude, java.math.BigDecimal longitude) {
        this.nameKo = name;
        this.code = code;
    }
}

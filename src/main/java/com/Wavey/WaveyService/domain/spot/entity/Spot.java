package com.Wavey.WaveyService.domain.spot.entity;

import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.enums.SpotSourceType;
import com.Wavey.WaveyService.global.common.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "spots",
        indexes = {
                @Index(name = "idx_spots_region_id", columnList = "region_id"),
                @Index(name = "idx_spots_category", columnList = "category"),
                @Index(name = "idx_spots_location", columnList = "latitude, longitude"),
                @Index(name = "idx_spots_source_external", columnList = "source_type, external_content_id")
        }
)
@AttributeOverride(name = "id", column = @Column(name = "spot_id"))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Spot extends BaseEntity {

    @Column(name = "region_id", nullable = false)
    private Long regionId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private SpotCategory category;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "opening_hours", length = 500)
    private String openingHours;

    @Column(name = "closed_days", length = 255)
    private String closedDays;

    @Column(name = "tel", length = 50)
    private String tel;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private SpotSourceType sourceType;

    @Column(name = "external_content_id", length = 100)
    private String externalContentId;

    @Builder.Default
    @Column(name = "avg_rating", nullable = false)
    private Double avgRating = 0.0;

    public Long getSpotId() {
        return getId();
    }

    public void updateThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public boolean updateFromExternal(
            Long regionId,
            String name,
            SpotCategory category,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            String description,
            String openingHours,
            String closedDays,
            String tel,
            String thumbnailUrl
    ) {
        boolean changed = false;

        if (!Objects.equals(this.regionId, regionId)) {
            this.regionId = regionId;
            changed = true;
        }
        if (!Objects.equals(this.name, name)) {
            this.name = name;
            changed = true;
        }
        if (!Objects.equals(this.category, category)) {
            this.category = category;
            changed = true;
        }
        if (!Objects.equals(this.address, address)) {
            this.address = address;
            changed = true;
        }
        if (isDifferentDecimal(this.latitude, latitude)) {
            this.latitude = latitude;
            changed = true;
        }
        if (isDifferentDecimal(this.longitude, longitude)) {
            this.longitude = longitude;
            changed = true;
        }
        if (!Objects.equals(this.description, description)) {
            this.description = description;
            changed = true;
        }
        if (!Objects.equals(this.openingHours, openingHours)) {
            this.openingHours = openingHours;
            changed = true;
        }
        if (!Objects.equals(this.closedDays, closedDays)) {
            this.closedDays = closedDays;
            changed = true;
        }
        if (!Objects.equals(this.tel, tel)) {
            this.tel = tel;
            changed = true;
        }
        if (thumbnailUrl != null && !Objects.equals(this.thumbnailUrl, thumbnailUrl)) {
            this.thumbnailUrl = thumbnailUrl;
            changed = true;
        }

        return changed;
    }

    public void update(
            String name,
            SpotCategory category,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            String description,
            String openingHours,
            String closedDays,
            String tel,
            String thumbnailUrl
    ) {
        this.name = name;
        this.category = category;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.openingHours = openingHours;
        this.closedDays = closedDays;
        this.tel = tel;
        this.thumbnailUrl = thumbnailUrl;
    }

    private boolean isDifferentDecimal(BigDecimal current, BigDecimal next) {
        if (current == null || next == null) {
            return !Objects.equals(current, next);
        }
        return current.compareTo(next) != 0;
    }
}

package com.Wavey.WaveyService.domain.spot.entity;

import com.Wavey.WaveyService.domain.spot.enums.PlaceType;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.global.common.BaseEntity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table
@AttributeOverride(name = "id", column = @Column(name = "spot_id"))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Spot extends BaseEntity {

    @Column(name = "region_id", nullable = false) private Long regionId;

    @Column(name = "name_ko", nullable = false, length = 255) private String nameKo;
    @Column(name = "name_en", length = 255) private String nameEn;

    @Enumerated(EnumType.STRING) @Column(name = "category", nullable = false, length = 30) private SpotCategory category;
    @Enumerated(EnumType.STRING) @Column(name = "place_type", nullable = false, length = 50) private PlaceType placeType;

    @Column(name = "description_ko", columnDefinition = "TEXT") private String descriptionKo;
    @Column(name = "description_en", columnDefinition = "TEXT") private String descriptionEn;

    @Column(name = "opening_hours", length = 500) private String openingHours;
    @Column(name = "break_time", length = 100) private String breakTime;

    @Column(name = "closed_days_ko", length = 255) private String closedDaysKo;
    @Column(name = "closed_days_en", length = 255) private String closedDaysEn;

    @Column(name = "tel", length = 50) private String tel;

    @Column(name = "address_ko", nullable = false, length = 500) private String addressKo;
    @Column(name = "address_en", length = 500) private String addressEn;

    @Column(name = "transport_info_ko", length = 500) private String transportInfoKo;
    @Column(name = "transport_info_en", length = 500) private String transportInfoEn;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 8) private BigDecimal latitude;
    @Column(name = "longitude", nullable = false, precision = 11, scale = 8) private BigDecimal longitude;

    @Column(name = "image_url", length = 500) private String imageUrl;

    @Builder.Default @Column(name = "avg_rating", nullable = false) private Double avgRating = 0.0;
    @Builder.Default @Column(name = "review_count", nullable = false) private long reviewCount = 0L;
    @Builder.Default @Column(name = "saved_count", nullable = false) private long savedCount = 0L;

    @Builder.Default
    @Column(nullable = false)
    private long reviewCount = 0;

    public Long getSpotId() {
        return getId();
    }

    public void update(
            Long regionId,
            String nameKo,
            String nameEn,
            SpotCategory category,
            PlaceType placeType,
            String descriptionKo,
            String descriptionEn,
            String openingHours,
            String breakTime,
            String closedDaysKo,
            String closedDaysEn,
            String tel,
            String addressKo,
            String addressEn,
            String transportInfoKo,
            String transportInfoEn,
            BigDecimal latitude,
            BigDecimal longitude,
            String imageUrl
    ) {
        this.regionId = regionId;
        this.nameKo = nameKo;
        this.nameEn = nameEn;
        this.category = category;
        this.placeType = placeType;
        this.descriptionKo = descriptionKo;
        this.descriptionEn = descriptionEn;
        this.openingHours = openingHours;
        this.breakTime = breakTime;
        this.closedDaysKo = closedDaysKo;
        this.closedDaysEn = closedDaysEn;
        this.tel = tel;
        this.addressKo = addressKo;
        this.addressEn = addressEn;
        this.transportInfoKo = transportInfoKo;
        this.transportInfoEn = transportInfoEn;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
    }

    public void updateRating(Double average, long count) {
        this.avgRating = average == null ? 0.0 : average;
        this.reviewCount = count;
    }

    public boolean updateFromExternal(
            Long regionId,
            String nameKo,
            String placeType,
            SpotCategory category,
            String addressKo,
            BigDecimal latitude,
            BigDecimal longitude,
            String descriptionKo,
            String openingHours,
            String breakTime,
            String closedDaysKo,
            String tel,
            String imageUrl
    ) {
        boolean changed = false;

        if (!Objects.equals(this.regionId, regionId)) {
            this.regionId = regionId;
            changed = true;
        }

        if (!Objects.equals(this.nameKo, nameKo)) {
            this.nameKo = nameKo;
            changed = true;
        }

        PlaceType normalizedPlaceType = PlaceType.from(placeType);
        if (!Objects.equals(this.placeType, normalizedPlaceType)) {
            this.placeType = normalizedPlaceType;
            changed = true;
        }

        if (!Objects.equals(this.category, category)) {
            this.category = category;
            changed = true;
        }

        if (!Objects.equals(this.addressKo, addressKo)) {
            this.addressKo = addressKo;
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

        if (!Objects.equals(this.descriptionKo, descriptionKo)) {
            this.descriptionKo = descriptionKo;
            changed = true;
        }

        if (!Objects.equals(this.openingHours, openingHours)) {
            this.openingHours = openingHours;
            changed = true;
        }

        if (!Objects.equals(this.breakTime, breakTime)) {
            this.breakTime = breakTime;
            changed = true;
        }

        if (!Objects.equals(this.closedDaysKo, closedDaysKo)) {
            this.closedDaysKo = closedDaysKo;
            changed = true;
        }

        if (!Objects.equals(this.tel, tel)) {
            this.tel = tel;
            changed = true;
        }

        if (imageUrl != null && !Objects.equals(this.imageUrl, imageUrl)) {
            this.imageUrl = imageUrl;
            changed = true;
        }

        return changed;
    }

    public void updateRating(double avgRating, long reviewCount) {
        this.avgRating = avgRating;
        this.reviewCount = reviewCount;
    }

    public void changeSavedCount(int delta) {
        this.savedCount = Math.max(0L, this.savedCount + delta);
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    private boolean isDifferentDecimal(BigDecimal current, BigDecimal next) {
        if (current == null || next == null) {
            return !Objects.equals(current, next);
        }

        return current.compareTo(next) != 0;
    }
}
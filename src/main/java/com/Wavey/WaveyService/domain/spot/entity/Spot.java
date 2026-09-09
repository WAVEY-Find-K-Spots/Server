package com.Wavey.WaveyService.domain.spot.entity;

import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.enums.SpotSourceType;
import com.Wavey.WaveyService.domain.spot.enums.PlaceType;
import com.Wavey.WaveyService.global.common.BaseEntity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(
        name = "spots",
        indexes = {
            @Index(name = "idx_spots_region_id", columnList = "region_id"),
            @Index(name = "idx_spots_category", columnList = "category"),
            @Index(name = "idx_spots_location", columnList = "latitude, longitude"),
            @Index(
                    name = "idx_spots_source_external",
                    columnList = "source_type, external_content_id")
        })
@AttributeOverride(name = "id", column = @Column(name = "spot_id"))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Spot extends BaseEntity {

    @Column(name = "region_id", nullable = false)
    private Long regionId;

    @Column(name = "media_type", length = 50)
    private String mediaType;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "place_type", length = 100)
    @Enumerated(EnumType.STRING)
    private PlaceType placeType;

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

    @Column(name = "break_time", length = 100)
    private String breakTime;

    @Column(name = "closed_days", length = 255)
    private String closedDays;

    @Column(name = "tel", length = 50)
    private String tel;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "source_updated_at")
    private LocalDate sourceUpdatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private SpotSourceType sourceType;

    @Column(name = "external_content_id", length = 100)
    private String externalContentId;

    @Builder.Default
    @Column(name = "avg_rating", nullable = false)
    private Double avgRating = 0.0;

    @Column(length = 255)
    private String nameEn;

    @Column(length = 500)
    private String addressEn;

    @Column(columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(length = 500)
    private String transportInfo;

    @Column(length = 500)
    private String transportInfoEn;

    @Column(length = 500)
    private String openingHoursEn;

    private String closedDaysEn;

    @Builder.Default
    @Column(nullable = false)
    private long reviewCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private long savedCount = 0;

    public void updateRating(double rating, long count) {
        this.avgRating = rating;
        this.reviewCount = count;
    }

    public void changeSavedCount(int delta) {
        this.savedCount = Math.max(0, this.savedCount + delta);
    }

    public Long getSpotId() {
        return getId();
    }

    public void updateThumbnailUrl(String thumbnailUrl) {
        this.imageUrl = thumbnailUrl;
    }

    public String getThumbnailUrl() {
        return imageUrl;
    }

    public boolean updateFromExternal(
            Long regionId,
            String mediaType,
            String title,
            String name,
            String placeType,
            SpotCategory category,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            String description,
            String openingHours,
            String breakTime,
            String closedDays,
            String tel,
            String thumbnailUrl,
            LocalDate sourceUpdatedAt) {
        boolean changed = false;

        if (!Objects.equals(this.regionId, regionId)) {
            this.regionId = regionId;
            changed = true;
        }
        if (!Objects.equals(this.mediaType, mediaType)) {
            this.mediaType = mediaType;
            changed = true;
        }
        if (!Objects.equals(this.title, title)) {
            this.title = title;
            changed = true;
        }
        if (!Objects.equals(this.name, name)) {
            this.name = name;
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
        if (!Objects.equals(this.breakTime, breakTime)) {
            this.breakTime = breakTime;
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
        if (thumbnailUrl != null && !Objects.equals(this.imageUrl, thumbnailUrl)) {
            this.imageUrl = thumbnailUrl;
            changed = true;
        }
        if (!Objects.equals(this.sourceUpdatedAt, sourceUpdatedAt)) {
            this.sourceUpdatedAt = sourceUpdatedAt;
            changed = true;
        }

        return changed;
    }

    public void update(
            String mediaType,
            String title,
            String name,
            String placeType,
            SpotCategory category,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            String description,
            String openingHours,
            String breakTime,
            String closedDays,
            String tel,
            String thumbnailUrl,
            LocalDate sourceUpdatedAt) {
        this.mediaType = mediaType;
        this.title = title;
        this.name = name;
        this.placeType = PlaceType.from(placeType);
        this.category = category;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.openingHours = openingHours;
        this.breakTime = breakTime;
        this.closedDays = closedDays;
        this.tel = tel;
        this.imageUrl = thumbnailUrl;
        this.sourceUpdatedAt = sourceUpdatedAt;
    }

    private boolean isDifferentDecimal(BigDecimal current, BigDecimal next) {
        if (current == null || next == null) {
            return !Objects.equals(current, next);
        }
        return current.compareTo(next) != 0;
    }
}

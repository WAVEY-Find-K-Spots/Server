package com.Wavey.WaveyService.domain.spot.dto.response;

import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.enums.SpotSourceType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotResponse {

    private String categoryCode;
    private String categoryLabel;
    private String transportInfo;
    private String playlistUrl;
    private String mapProvider;
    private String googleMapsUrl;
    private long reviewCount;
    private boolean saved;
    private boolean stampAcquired;
    private List<Long> routeIds;
    private List<String> tags;
    private Long spotId;
    private Long regionId;
    private String mediaType;
    private String title;
    private String name;
    private String placeType;
    private SpotCategory category;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private String openingHours;
    private String breakTime;
    private String closedDays;
    private String tel;
    private String thumbnailUrl;
    private LocalDate sourceUpdatedAt;
    private SpotSourceType sourceType;
    private String externalContentId;
    private Double avgRating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

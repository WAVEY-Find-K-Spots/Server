package com.Wavey.WaveyService.domain.spot.dto.response;

import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotListResponse {

    private String description;
    private String categoryCode;
    private String categoryLabel;
    private Double distanceMeters;
    private long reviewCount;
    private boolean saved;
    private Long regionId;
    private Long spotId;
    private String name;
    private SpotCategory category;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String thumbnailUrl;
    private Double avgRating;
}

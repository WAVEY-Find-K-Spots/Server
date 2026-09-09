package com.Wavey.WaveyService.domain.spot.external.dto;

import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.enums.SpotSourceType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExternalSpotPayload {

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

    public ExternalSpotPayload withThumbnailUrl(String thumbnailUrl) {
        return this.toBuilder().thumbnailUrl(thumbnailUrl).build();
    }
}

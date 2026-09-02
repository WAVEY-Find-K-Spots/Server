package com.Wavey.WaveyService.domain.spot.dto.response;

import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.enums.SpotSourceType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotResponse {

    private Long spotId;
    private Long regionId;
    private String name;
    private SpotCategory category;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private String openingHours;
    private String closedDays;
    private String tel;
    private String thumbnailUrl;
    private SpotSourceType sourceType;
    private String externalContentId;
    private Double avgRating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

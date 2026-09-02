package com.Wavey.WaveyService.domain.spot.dto.response;

import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotListResponse {

    private Long spotId;
    private String name;
    private SpotCategory category;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String thumbnailUrl;
    private Double avgRating;
}

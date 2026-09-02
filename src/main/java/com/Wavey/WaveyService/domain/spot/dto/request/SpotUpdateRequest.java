package com.Wavey.WaveyService.domain.spot.dto.request;

import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotUpdateRequest {

    @Pattern(regexp = "^(?!\\s*$).+")
    private String name;

    private SpotCategory category;

    private String address;

    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private BigDecimal longitude;

    private String description;

    private String openingHours;

    private String closedDays;

    private String tel;

    private String thumbnailUrl;
}

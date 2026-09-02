package com.Wavey.WaveyService.domain.spot.dto.request;

import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.enums.SpotSourceType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotCreateRequest {

    @NotNull
    private Long regionId;

    @NotBlank
    private String name;

    @NotNull
    private SpotCategory category;

    private String address;

    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private BigDecimal latitude;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private BigDecimal longitude;

    private String description;

    private String openingHours;

    private String closedDays;

    private String tel;

    private String thumbnailUrl;

    @NotNull
    private SpotSourceType sourceType;

    private String externalContentId;
}

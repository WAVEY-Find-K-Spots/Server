package com.Wavey.WaveyService.domain.spot.dto.request;

import com.Wavey.WaveyService.domain.spot.enums.PlaceType;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SpotUpdateRequest(

        @Positive Long regionId,

        @Pattern(regexp = ".*\\S.*")
        @Size(max = 255)
        String nameKo,

        @Size(max = 255) String nameEn,

        SpotCategory category,

        PlaceType placeType,

        String descriptionKo,

        String descriptionEn,

        @Size(max = 500) String openingHours,

        @Size(max = 100) String breakTime,

        @Size(max = 255) String closedDaysKo,

        @Size(max = 255) String closedDaysEn,

        @Size(max = 50) String tel,

        @Size(max = 500) String addressKo,

        @Size(max = 500) String addressEn,

        @Size(max = 500) String transportInfoKo,

        @Size(max = 500) String transportInfoEn,

        @DecimalMin("33.0") @DecimalMax("38.7")
        BigDecimal latitude,

        @DecimalMin("124.5") @DecimalMax("132.0")
        BigDecimal longitude,

        @Size(max = 500) String imageUrl
) {}
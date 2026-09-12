package com.Wavey.WaveyService.domain.spot.dto.request;

import com.Wavey.WaveyService.domain.spot.enums.PlaceType;
import com.Wavey.WaveyService.domain.spot.enums.SortBy;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SpotSearchRequest(

        @Size(max = 200)
        String keyword,

        @Positive
        Long regionId,

        SpotCategory category,

        PlaceType placeType,

        @DecimalMin("0.0")
        @DecimalMax("5.0")
        Double minRating,

        @DecimalMin("33.0")
        @DecimalMax("38.7")
        BigDecimal latitude,

        @DecimalMin("124.5")
        @DecimalMax("132.0")
        BigDecimal longitude,

        @DecimalMin("1.0")
        @DecimalMax("100000.0")
        Double radiusMeters,

        SortBy sort,

        @Min(0)
        Integer page
) {

    public SpotSearchRequest {
        sort = sort == null ? SortBy.POPULAR : sort;
        page = page == null ? 0 : page;
    }
}
package com.Wavey.WaveyService.domain.spot.dto.request;

import jakarta.validation.constraints.*;

import lombok.*;

@Getter
@Setter
public class SpotSearchRequest {
    @Size(max = 200)
    private String keyword;

    private Long regionId;
    private String category;

    @DecimalMin("0")
    @DecimalMax("5")
    private Double minRating;

    private Double latitude;
    private Double longitude;

    @DecimalMin("1")
    @DecimalMax("100000")
    private Double radiusMeters;

    private SortBy sort = SortBy.POPULAR;

    @Min(0)
    @Max(10000)
    private int page = 0;

    @Min(1)
    @Max(100)
    private int size = 20;

    private String language;

    public enum SortBy {
        POPULAR,
        RATING,
        LATEST,
        DISTANCE
    }
}

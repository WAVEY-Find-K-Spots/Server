package com.Wavey.WaveyService.domain.route.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "GeoJSON LineString ([경도, 위도] 순서)")
public record GeoLineString(
        @Schema(description = "geometry 타입", example = "LineString")
        String type,

        @Schema(description = "좌표 배열 ([경도, 위도])")
        List<List<Double>> coordinates
) {
    public static GeoLineString of(List<List<Double>> coordinates) {
        return new GeoLineString("LineString", coordinates);
    }
}

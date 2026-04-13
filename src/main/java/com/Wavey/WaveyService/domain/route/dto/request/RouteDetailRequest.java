package com.Wavey.WaveyService.domain.route.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class RouteDetailRequest {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddSpot {
        @Schema(description = "추가할 스팟 ID", example = "10")
        private Long spot_id;
    }

    @Getter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class ReorderSpots {
        @Schema(description = "재정렬된 RouteSpot ID 리스트 (순서대로)", example = "[3, 1, 2]")
        private List<Long> route_spot_ids;
    }
}
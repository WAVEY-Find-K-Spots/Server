package com.Wavey.WaveyService.domain.route.dto.response;

import com.Wavey.WaveyService.domain.route.entity.RouteSpot;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "루트 스팟 응답")
@Getter
@Builder
public class RouteSpotResponse {

    @Schema(description = "루트 스팟 ID", example = "10")
    private Long routeSpotId;

    @Schema(description = "스팟 ID", example = "101")
    private Long spotId;

    @Schema(description = "순서", example = "1")
    private Integer sequenceOrder;

    public static RouteSpotResponse from(RouteSpot routeSpot) {
        return RouteSpotResponse.builder()
                .routeSpotId(routeSpot.getId())
                .spotId(routeSpot.getSpotId())
                .sequenceOrder(routeSpot.getSequenceOrder())
                .build();
    }
}
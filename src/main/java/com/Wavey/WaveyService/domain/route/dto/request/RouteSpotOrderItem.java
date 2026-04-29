package com.Wavey.WaveyService.domain.route.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "스팟 순서 항목")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RouteSpotOrderItem {

    @Schema(description = "루트 스팟 ID", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long routeSpotId;

    @Schema(description = "변경할 순서", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Min(1)
    private Integer sequenceOrder;
}
package com.Wavey.WaveyService.domain.route.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "스팟 추가 항목")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RouteSpotRequest {

    @Schema(description = "스팟 ID", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long spotId;

    @Schema(description = "루트 내 순서 (1부터 시작)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Min(1)
    private Integer sequenceOrder;
}

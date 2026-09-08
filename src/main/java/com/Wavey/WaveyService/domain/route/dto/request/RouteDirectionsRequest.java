package com.Wavey.WaveyService.domain.route.dto.request;

import com.Wavey.WaveyService.domain.route.entity.TransportMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "루트 경로 계산 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RouteDirectionsRequest {

    @Schema(description = "이동수단 (WALK / TRANSIT / CAR)", example = "TRANSIT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private TransportMode transportMode;
}

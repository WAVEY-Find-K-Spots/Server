package com.Wavey.WaveyService.domain.route.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "스팟 순서 일괄 변경 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RouteSpotReorderRequest {

    @Schema(description = "재정렬할 스팟 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private List<RouteSpotOrderItem> spots;
}

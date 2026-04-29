package com.Wavey.WaveyService.domain.route.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "루트 스팟 추가 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RouteSpotAddRequest {

    @Schema(description = "추가할 스팟 ID", example = "303", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long spotId;

    @Schema(description = "삽입 위치 순서. 이후 스팟은 자동으로 밀림", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Min(1)
    private Integer sequenceOrder;
}

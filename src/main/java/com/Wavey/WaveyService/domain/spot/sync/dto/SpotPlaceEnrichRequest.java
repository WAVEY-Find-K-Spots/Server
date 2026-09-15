package com.Wavey.WaveyService.domain.spot.sync.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "장소명 기반 Spot 보강 요청")
public class SpotPlaceEnrichRequest {
    @NotBlank
    @Schema(description = "TourAPI에서 검색할 장소명", example = "경복궁", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
}

package com.Wavey.WaveyService.domain.spot.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Google Places 이미지 백필 결과")
public class SpotPlacesEnrichResponse {

    @Schema(description = "이번 요청에서 처리 시도한 스팟 수", example = "50")
    private int requested;

    @Schema(description = "실제로 imageUrl이 채워진 스팟 수", example = "31")
    private int filled;

    @Schema(description = "검색 결과 없음/사진 없음으로 건너뜀", example = "19")
    private int skipped;

    @Schema(description = "이번 호출 전 남아있던 이번 달 Photo 예산", example = "1000")
    private int budgetBefore;

    @Schema(description = "이번 호출 후 남은 이번 달 Photo 예산", example = "969")
    private int budgetAfter;

    @Schema(description = "월 예산 소진으로 중단됐는지 여부", example = "false")
    private boolean stoppedByBudget;
}

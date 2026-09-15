package com.Wavey.WaveyService.domain.spot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "찜(저장) 토글 결과")
public record SpotSaveResponse(
        @Schema(description = "스팟 ID", example = "1") Long spotId,
        @Schema(description = "저장 여부(토글 후 최종 상태)", example = "true") boolean saved,
        @Schema(description = "해당 스팟을 저장한 유저 수", example = "42") long savedCount
) {
}

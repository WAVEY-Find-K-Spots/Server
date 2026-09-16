package com.Wavey.WaveyService.domain.spot.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Wikimedia Commons 이미지 백필 결과")
public class SpotWikimediaEnrichResponse {

    @Schema(description = "이번 요청에서 처리 시도한 스팟 수", example = "50")
    private int requested;

    @Schema(description = "실제로 imageUrl이 채워진 스팟 수", example = "12")
    private int filled;

    @Schema(description = "검색 결과 없음/재사용 가능 라이선스 없음/미지원 이미지 형식으로 건너뜀", example = "38")
    private int skipped;
}

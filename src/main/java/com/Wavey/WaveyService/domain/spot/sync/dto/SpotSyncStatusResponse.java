package com.Wavey.WaveyService.domain.spot.sync.dto;

import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Spot Sync 상태")
public class SpotSyncStatusResponse {
    @Schema(description = "카운터 기준 날짜", example = "2026-09-15")
    private LocalDate date;
    @Schema(description = "TourAPI 일일 호출 제한", example = "1000")
    private int dailyCallLimit;
    @Schema(description = "안전 여유 호출 수", example = "50")
    private int safetyReserve;
    @Schema(description = "오늘 사용한 호출 수", example = "4")
    private int apiCallCount;
    @Schema(description = "남은 호출 수", example = "996")
    private int remainingCalls;
    @Schema(description = "다음 heritage 동기화 페이지", example = "2")
    private int nextHeritagePage;
}

package com.Wavey.WaveyService.domain.spot.sync.dto;

import lombok.Builder;
import lombok.Getter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Builder
@Schema(description = "Spot Sync 실행 결과")
public class SpotSyncResponse {
    @Schema(description = "외부 API 호출 여부", example = "true")
    private boolean externalApiCalled;
    @Schema(description = "이번 요청에서 사용한 외부 API 호출 수", example = "4")
    private int apiCallCount;
    @Schema(description = "처리 요청 레코드 수", example = "1")
    private int requested;
    @Schema(description = "외부 API 전체 후보 수", example = "12")
    private int total;
    @Schema(description = "처리 페이지 수", example = "1")
    private int pages;
    @Schema(description = "새로 저장한 Spot 수", example = "1")
    private int inserted;
    @Schema(description = "수정한 Spot 수", example = "0")
    private int updated;
    @Schema(description = "변경 없는 Spot 수", example = "0")
    private int unchanged;
    @Schema(description = "건너뛴 수", example = "0")
    private int skipped;
    @Schema(description = "실패 수", example = "0")
    private int failed;
    @Schema(description = "일일 API 예산 안전 여유 도달로 중단 여부", example = "false")
    private boolean stoppedByDailyLimit;
    @Schema(description = "해결 또는 생성한 Content 수", example = "1")
    private int contentsResolved;
    @Schema(description = "새로 생성한 SpotContent 링크 수", example = "1")
    private int linksCreated;
}

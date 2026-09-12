package com.Wavey.WaveyService.domain.region.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Schema(description = "지역 응답")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionResponse {
    @Schema(description = "지역 ID", example = "1")
    private Long regionId;

    @Schema(description = "지역 한글명", example = "서울")
    private String nameKo;

    @Schema(description = "지역 영문명", example = "Seoul")
    private String nameEn;

    @Schema(description = "생성 일시", example = "2026-09-10T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2026-09-10T12:00:00")
    private LocalDateTime updatedAt;
}

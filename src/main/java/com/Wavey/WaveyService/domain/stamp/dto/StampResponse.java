package com.Wavey.WaveyService.domain.stamp.dto;

import com.Wavey.WaveyService.domain.stamp.entity.StampCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StampResponse {

    @Schema(description = "스탬프 ID", example = "1")
    private Long stampId;

    @Schema(description = "스팟 ID", example = "1")
    private Long spotId;

    @Schema(description = "스탬프 이미지 링크", example = "https://example.com/stamps/visit.png")
    private String imgLink;

    @Schema(description = "스탬프 카테고리", example = "방문")
    private StampCategory category;

    @Schema(description = "스탬프 이름", example = "성산일출봉 방문 스탬프")
    private String name;

    @Schema(description = "스탬프 설명", example = "성산일출봉 방문 시 획득 가능한 스탬프")
    private String context;

    @Schema(description = "스탬프 획득 조건", example = "해당 장소 방문 인증")
    private String requirement;

    @Schema(description = "생성 시각", example = "2026-05-10T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각", example = "2026-05-10T11:00:00")
    private LocalDateTime updatedAt;
}

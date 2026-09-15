package com.Wavey.WaveyService.domain.spot.sync.dto;

import com.Wavey.WaveyService.domain.content.entity.ContentCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "콘텐츠 제목 기반 SpotContent 자동 생성 검토 요청")
public class SpotTitleReviewRequest {
    @NotBlank
    @Schema(description = "콘텐츠 제목", example = "도깨비", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotNull
    @Schema(description = "콘텐츠 카테고리", example = "DRAMA", requiredMode = Schema.RequiredMode.REQUIRED)
    private ContentCategory category;
}

package com.Wavey.WaveyService.domain.content.dto;

import com.Wavey.WaveyService.domain.content.entity.Content;
import com.Wavey.WaveyService.domain.content.entity.ContentCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스팟에 연결된 콘텐츠 한 건")
public record SpotContentItemResponse(
        @Schema(description = "콘텐츠 ID", example = "1") Long contentId,
        @Schema(description = "콘텐츠 종류", example = "DRAMA") ContentCategory category,
        @Schema(description = "제목 (한글)", example = "도깨비") String title
) {
    public static SpotContentItemResponse from(Content content) {
        return new SpotContentItemResponse(content.getContentId(), content.getCategory(), content.getTitle());
    }
}

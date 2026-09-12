package com.Wavey.WaveyService.domain.content.dto;

import com.Wavey.WaveyService.domain.content.entity.Content;
import com.Wavey.WaveyService.domain.content.entity.ContentCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentResponse {

    @Schema(description = "작품 ID", example = "1")
    private Long contentId;

    @Schema(description = "한글 제목", example = "도깨비")
    private String titleKo;

    @Schema(description = "영문 제목", example = "Guardian", nullable = true)
    private String titleEn;

    @Schema(description = "작품 종류")
    private ContentCategory category;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static ContentResponse from(Content content) {
        return ContentResponse.builder()
                .contentId(content.getContentId())
                .titleKo(content.getTitle())
                .titleEn(content.getTitleEn())
                .category(content.getCategory())
                .createdAt(content.getCreatedAt())
                .updatedAt(content.getUpdatedAt())
                .build();
    }
}

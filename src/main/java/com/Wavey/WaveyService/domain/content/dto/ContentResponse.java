package com.Wavey.WaveyService.domain.content.dto;

import com.Wavey.WaveyService.domain.content.entity.ContentPlatform;
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
public class ContentResponse {

    @Schema(description = "콘텐츠 ID", example = "1")
    private Long contentId;

    @Schema(description = "플랫폼", example = "YOUTUBE")
    private ContentPlatform platform;

    @Schema(description = "제목", example = "밤양갱")
    private String title;

    @Schema(description = "콘텐츠 설명", example = "플레이리스트에 저장하고 싶은 콘텐츠")
    private String description;

    @Schema(description = "플랫폼 고유 ID", example = "dQw4w9WgXcQ")
    private String externalId;

    @Schema(description = "썸네일 URL", example = "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg")
    private String thumbnailUrl;

    @Schema(description = "생성 시각", example = "2026-03-23T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각", example = "2026-03-23T11:00:00")
    private LocalDateTime updatedAt;
}

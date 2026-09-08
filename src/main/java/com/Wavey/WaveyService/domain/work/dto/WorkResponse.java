package com.Wavey.WaveyService.domain.work.dto;

import com.Wavey.WaveyService.domain.work.entity.Work;
import com.Wavey.WaveyService.domain.work.entity.WorkType;
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
public class WorkResponse {

    @Schema(description = "작품 ID", example = "1")
    private Long workId;

    @Schema(description = "한글 제목", example = "도깨비")
    private String title;

    @Schema(description = "영문 제목", example = "Guardian", nullable = true)
    private String titleEn;

    @Schema(description = "작품 종류")
    private WorkType type;

    @Schema(description = "가수명", nullable = true)
    private String artistName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static WorkResponse from(Work work) {
        return WorkResponse.builder()
                .workId(work.getWorkId())
                .title(work.getTitle())
                .titleEn(work.getTitleEn())
                .type(work.getType())
                .artistName(work.getArtistName())
                .createdAt(work.getCreatedAt())
                .updatedAt(work.getUpdatedAt())
                .build();
    }
}

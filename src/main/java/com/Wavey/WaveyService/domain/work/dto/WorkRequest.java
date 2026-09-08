package com.Wavey.WaveyService.domain.work.dto;

import com.Wavey.WaveyService.domain.work.entity.WorkType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        requiredProperties = {"title", "type"},
        example = """
                {
                  "title": "작품 한글 제목 (필수)",
                  "titleEn": "작품 영문 제목 (선택)",
                  "type": "DRAMA | MOVIE | KPOP (필수)",
                  "artistName": "가수명. type이 KPOP일 때만 (선택)"
                }
                """
)
public class WorkRequest {

    @NotBlank
    @Schema(
            description = "한글 제목",
            example = "작품 한글 제목 (필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String title;

    @Schema(
            description = "영문 제목",
            example = "작품 영문 제목 (선택)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    private String titleEn;

    @NotNull
    @Schema(description = "작품 종류. DRAMA | MOVIE | KPOP", requiredMode = Schema.RequiredMode.REQUIRED)
    private WorkType type;

    @Schema(
            description = "가수명. type이 KPOP일 때만",
            example = "가수명. type이 KPOP일 때만 (선택)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    private String artistName;
}

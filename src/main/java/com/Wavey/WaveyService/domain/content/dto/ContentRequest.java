package com.Wavey.WaveyService.domain.content.dto;

import com.Wavey.WaveyService.domain.content.entity.ContentCategory;
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
        requiredProperties = {"titleKo", "category"},
        example = """
                {
                  "titleKo": "작품 한글 제목 (필수)",
                  "titleEn": "작품 영문 제목 (선택)",
                  "category": "ARTIST | DRAMA | MOVIE (필수)"
                }
                """
)
public class ContentRequest {

    @NotBlank
    @Schema(
            description = "한글 제목",
            example = "작품 한글 제목 (필수)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String titleKo;

    @Schema(
            description = "영문 제목",
            example = "작품 영문 제목 (선택)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    private String titleEn;

    @NotNull
    @Schema(description = "작품 종류. DRAMA | MOVIE | KPOP", requiredMode = Schema.RequiredMode.REQUIRED)
    private ContentCategory category;
}

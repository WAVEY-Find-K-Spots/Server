package com.Wavey.WaveyService.domain.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(example = """
        {
          "hidden": "true | false (필수). true=숨김, false=다시 공개"
        }
        """)
public class MediaVisibilityRequest {

    @NotNull
    @Schema(description = "true=숨김, false=다시 공개", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean hidden;
}

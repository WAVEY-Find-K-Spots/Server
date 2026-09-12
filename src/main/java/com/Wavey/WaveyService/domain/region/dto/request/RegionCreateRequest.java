package com.Wavey.WaveyService.domain.region.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "지역 생성 요청")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionCreateRequest {
    @Schema(description = "지역 한글명", example = "서울", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "지역 한글명은 필수입니다.")
    private String nameKo;

    @Schema(description = "지역 영문명", example = "Seoul", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "지역 영문명은 필수입니다.")
    private String nameEn;
}

package com.Wavey.WaveyService.domain.region.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "지역 수정 요청")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionUpdateRequest {
    @Schema(description = "지역 한글명", example = "서울")
    @NotBlank(message = "지역 한글명은 필수입니다.")
    private String nameKo;

    @Schema(description = "지역 영문명", example = "Seoul")
    @NotBlank(message = "지역 영문명은 필수입니다.")
    private String nameEn;
}

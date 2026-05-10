package com.Wavey.WaveyService.domain.stamp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StampRequest {

    @NotNull(message = "spotId는 필수입니다.")
    @Schema(description = "스팟 ID", example = "1")
    private Long spotId;

    @NotBlank(message = "이미지 링크는 필수입니다.")
    @Schema(description = "스탬프 이미지 링크", example = "https://example.com/stamps/visit.png")
    private String imgLink;

    @NotBlank(message = "카테고리는 필수입니다.")
    @Schema(description = "스탬프 카테고리", example = "방문", allowableValues = {"방문", "리뷰"})
    private String category;

    @NotBlank(message = "이름은 필수입니다.")
    @Schema(description = "스탬프 이름", example = "성산일출봉 방문 스탬프")
    private String name;

    @Schema(description = "스탬프 설명", example = "성산일출봉 방문 시 획득 가능한 스탬프")
    private String context;

    @Schema(description = "스탬프 획득 조건", example = "해당 장소 방문 인증")
    private String requirement;
}

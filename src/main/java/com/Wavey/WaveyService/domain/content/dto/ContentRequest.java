package com.Wavey.WaveyService.domain.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
public class ContentRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Schema(description = "콘텐츠 제목", example = "BTS - Dynamite")
    private String title;

    @NotBlank(message = "URL은 필수입니다.")
    @Schema(description = "유튜브 또는 스포티파이 URL", example = "https://www.youtube.com/watch?v=gdZLi9oWNZg")
    private String url;

    @Schema(description = "콘텐츠 설명", example = "플레이리스트에 저장하고 싶은 콘텐츠")
    private String description;
}

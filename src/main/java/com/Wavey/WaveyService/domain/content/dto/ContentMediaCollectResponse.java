package com.Wavey.WaveyService.domain.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentMediaCollectResponse {

    @Schema(example = "1")
    private Long contentId;

    private MediaCollectResponse videos;

    private MediaCollectResponse tracks;
}

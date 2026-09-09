package com.Wavey.WaveyService.domain.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaCollectResponse {

    @Schema(example = "1")
    private Long contentId;

    @Schema(description = "저장된 개수")
    private int saved;

    @Schema(description = "탈락한 개수")
    private int dropped;

    @Schema(nullable = true)
    private List<ContentVideoResponse> videos;

    @Schema(nullable = true)
    private List<ContentTrackResponse> tracks;
}

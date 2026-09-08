package com.Wavey.WaveyService.domain.content.dto;

import com.Wavey.WaveyService.domain.content.entity.WorkVideo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkVideoResponse {

    @Schema(description = "DB ID", example = "1")
    private Long id;

    @Schema(description = "YouTube videoId", example = "dQw4w9WgXcQ")
    private String videoId;

    private String title;

    @Schema(nullable = true)
    private String channelTitle;

    private String thumbnailUrl;

    @Schema(description = "길이(초)")
    private Integer durationSec;

    @Schema(allowableValues = {"SHORT", "LONG"})
    private String kind;

    private boolean hidden;

    public static WorkVideoResponse from(WorkVideo video) {
        return WorkVideoResponse.builder()
                .id(video.getWorkVideoId())
                .videoId(video.getYoutubeVideoId())
                .title(video.getTitle())
                .channelTitle(video.getChannelTitle())
                .thumbnailUrl(video.getThumbnailUrl())
                .durationSec(video.getDurationSec())
                .kind(video.kind())
                .hidden(video.isHidden())
                .build();
    }
}

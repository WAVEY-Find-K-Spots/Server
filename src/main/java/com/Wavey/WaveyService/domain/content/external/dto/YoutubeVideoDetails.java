package com.Wavey.WaveyService.domain.content.external.dto;

public record YoutubeVideoDetails(
        String videoId,
        String title,
        String description,
        String thumbnailUrl,
        String channelTitle,
        Integer durationSec
) {
}

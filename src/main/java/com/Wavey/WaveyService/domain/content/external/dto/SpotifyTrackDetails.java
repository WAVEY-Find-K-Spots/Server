package com.Wavey.WaveyService.domain.content.external.dto;

public record SpotifyTrackDetails(
        String trackId,
        String title,
        String artistName,
        String albumName,
        String thumbnailUrl
) {
}

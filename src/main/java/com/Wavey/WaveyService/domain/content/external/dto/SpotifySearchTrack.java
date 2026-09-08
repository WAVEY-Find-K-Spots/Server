package com.Wavey.WaveyService.domain.content.external.dto;

public record SpotifySearchTrack(
        String trackId,
        String title,
        String artistName,
        String albumId,
        String albumName,
        String thumbnailUrl,
        Long durationMs,
        String previewUrl,
        String spotifyUrl
) {
}

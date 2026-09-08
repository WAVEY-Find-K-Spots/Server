package com.Wavey.WaveyService.domain.content.external.dto;

import java.util.List;

public record SpotifyAlbumTracks(
        String albumId,
        String name,
        String imageUrl,
        List<SpotifySearchTrack> tracks
) {
}

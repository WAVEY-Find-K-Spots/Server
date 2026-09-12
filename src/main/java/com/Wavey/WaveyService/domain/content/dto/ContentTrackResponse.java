package com.Wavey.WaveyService.domain.content.dto;

import com.Wavey.WaveyService.domain.content.entity.ContentTrack;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentTrackResponse {

    @Schema(description = "DB ID", example = "1")
    private Long id;

    @Schema(description = "소속 앨범 DB ID. 단독 트랙이면 null", nullable = true)
    private Long contentAlbumId;

    @Schema(description = "Spotify trackId")
    private String spotifyTrackId;

    private String title;

    @Schema(nullable = true)
    private String artistName;

    @Schema(nullable = true)
    private String imageUrl;

    private String spotifyUrl;

    @Schema(nullable = true)
    private Long durationMs;

    private boolean hidden;

    public static ContentTrackResponse from(ContentTrack track) {
        return ContentTrackResponse.builder()
                .id(track.getContentTrackId())
                .contentAlbumId(track.getContentAlbumId())
                .spotifyTrackId(track.getSpotifyTrackId())
                .title(track.getTitle())
                .artistName(track.getArtistName())
                .imageUrl(track.getImageUrl())
                .spotifyUrl(track.getSpotifyUrl())
                .durationMs(track.getDurationMs())
                .hidden(track.isHidden())
                .build();
    }
}

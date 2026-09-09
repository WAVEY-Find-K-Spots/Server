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

    @Schema(description = "Spotify trackId")
    private String spotifyId;

    private String name;

    @Schema(nullable = true)
    private String artistName;

    @Schema(nullable = true)
    private String albumName;

    @Schema(nullable = true)
    private String imageUrl;

    @Schema(description = "미리듣기 URL. 없으면 null", nullable = true)
    private String previewUrl;

    @Schema(description = "미리듣기 가능 여부")
    private boolean previewAvailable;

    private String spotifyUrl;

    @Schema(nullable = true)
    private Long durationMs;

    private boolean hidden;

    public static ContentTrackResponse from(ContentTrack track) {
        return ContentTrackResponse.builder()
                .id(track.getContentTrackId())
                .spotifyId(track.getSpotifyId())
                .name(track.getName())
                .artistName(track.getArtistName())
                .albumName(track.getAlbumName())
                .imageUrl(track.getImageUrl())
                .previewUrl(track.getPreviewUrl())
                .previewAvailable(track.isPreviewAvailable())
                .spotifyUrl(track.getSpotifyUrl())
                .durationMs(track.getDurationMs())
                .hidden(track.isHidden())
                .build();
    }
}

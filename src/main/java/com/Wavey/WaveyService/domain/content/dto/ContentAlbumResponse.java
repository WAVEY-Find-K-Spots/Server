package com.Wavey.WaveyService.domain.content.dto;

import com.Wavey.WaveyService.domain.content.entity.ContentAlbum;
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
public class ContentAlbumResponse {

    @Schema(description = "DB ID", example = "1")
    private Long id;

    @Schema(description = "Spotify albumId")
    private String spotifyAlbumId;

    private String title;

    @Schema(nullable = true)
    private String imageUrl;

    private String spotifyUrl;

    private boolean hidden;

    @Schema(description = "앨범 수록 트랙. 목록 조회 시 포함")
    @Builder.Default
    private List<ContentTrackResponse> tracks = List.of();

    public static ContentAlbumResponse from(ContentAlbum album) {
        return from(album, List.of());
    }

    public static ContentAlbumResponse from(ContentAlbum album, List<ContentTrackResponse> tracks) {
        return ContentAlbumResponse.builder()
                .id(album.getContentAlbumId())
                .spotifyAlbumId(album.getSpotifyAlbumId())
                .title(album.getTitle())
                .imageUrl(album.getImageUrl())
                .spotifyUrl(album.getSpotifyUrl())
                .hidden(album.isHidden())
                .tracks(tracks == null ? List.of() : tracks)
                .build();
    }
}

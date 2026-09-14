package com.Wavey.WaveyService.domain.content.entity;

import com.Wavey.WaveyService.global.common.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "content_tracks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_content_tracks_content_spotify_track",
                columnNames = {"content_id", "spotify_track_id"}
        )
)
@AttributeOverride(name = "id", column = @Column(name = "content_track_id"))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentTrack extends BaseEntity {

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "content_album_id")
    private Long contentAlbumId;

    @Column(name = "spotify_track_id", nullable = false, length = 64)
    private String spotifyTrackId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "artist_name", length = 255)
    private String artistName;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "spotify_url", length = 500)
    private String spotifyUrl;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Builder.Default
    @Column(nullable = false)
    private boolean hidden = false;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    public Long getContentTrackId() {
        return getId();
    }

    public void updateFetched(
            Long contentAlbumId,
            String title,
            String artistName,
            String imageUrl,
            String spotifyUrl,
            Long durationMs
    ) {
        this.contentAlbumId = contentAlbumId;
        this.title = title;
        this.artistName = artistName;
        this.imageUrl = imageUrl;
        this.spotifyUrl = spotifyUrl;
        this.durationMs = durationMs;
        this.fetchedAt = LocalDateTime.now();
    }

    public void updateHidden(boolean hidden) {
        this.hidden = hidden;
    }
}

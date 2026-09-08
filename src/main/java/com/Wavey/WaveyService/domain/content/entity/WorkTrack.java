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
        name = "work_tracks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_work_tracks_work_spotify",
                columnNames = {"work_id", "spotify_id"}
        )
)
@AttributeOverride(name = "id", column = @Column(name = "work_track_id"))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkTrack extends BaseEntity {

    @Column(name = "work_id", nullable = false)
    private Long workId;

    @Column(name = "spotify_id", nullable = false, length = 64)
    private String spotifyId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "artist_name", length = 255)
    private String artistName;

    @Column(name = "album_name", length = 255)
    private String albumName;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "preview_url", length = 1000)
    private String previewUrl;

    @Column(name = "spotify_url", length = 500)
    private String spotifyUrl;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Builder.Default
    @Column(nullable = false)
    private boolean hidden = false;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    public Long getWorkTrackId() {
        return getId();
    }

    public void updateFetched(
            String name,
            String artistName,
            String albumName,
            String imageUrl,
            String previewUrl,
            String spotifyUrl,
            Long durationMs
    ) {
        this.name = name;
        this.artistName = artistName;
        this.albumName = albumName;
        this.imageUrl = imageUrl;
        this.previewUrl = previewUrl;
        this.spotifyUrl = spotifyUrl;
        this.durationMs = durationMs;
        this.fetchedAt = LocalDateTime.now();
    }

    public void updateHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isPreviewAvailable() {
        return previewUrl != null && !previewUrl.isBlank();
    }
}

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
        name = "content_albums",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_content_albums_content_spotify",
                columnNames = {"content_id", "spotify_album_id"}
        )
)
@AttributeOverride(name = "id", column = @Column(name = "content_album_id"))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentAlbum extends BaseEntity {

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "spotify_album_id", nullable = false, length = 64)
    private String spotifyAlbumId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "spotify_url", length = 500)
    private String spotifyUrl;

    @Builder.Default
    @Column(nullable = false)
    private boolean hidden = false;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    public Long getContentAlbumId() {
        return getId();
    }

    public void updateFetched(String title, String imageUrl, String spotifyUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.spotifyUrl = spotifyUrl;
        this.fetchedAt = LocalDateTime.now();
    }

    public void updateHidden(boolean hidden) {
        this.hidden = hidden;
    }
}

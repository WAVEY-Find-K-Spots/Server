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
        name = "content_videos",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_work_videos_work_youtube",
                columnNames = {"content_id", "youtube_video_id"}
        )
)
@AttributeOverride(name = "id", column = @Column(name = "content_video_id"))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentVideo extends BaseEntity {

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "youtube_video_id", nullable = false, length = 32)
    private String youtubeVideoId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "channel_title", length = 255)
    private String channelTitle;

    @Column(name = "thumbnail_url", nullable = false, length = 500)
    private String thumbnailUrl;

    @Column(name = "duration_sec", nullable = false)
    private int durationSec;

    @Builder.Default
    @Column(nullable = false)
    private boolean hidden = false;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    public Long getContentVideoId() {
        return getId();
    }

    public void updateFetched(String title, String channelTitle, String thumbnailUrl, int durationSec) {
        this.title = title;
        this.channelTitle = channelTitle;
        this.thumbnailUrl = thumbnailUrl;
        this.durationSec = durationSec;
        this.fetchedAt = LocalDateTime.now();
    }

    public void updateHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String kind() {
        return durationSec <= 60 ? "SHORT" : "LONG";
    }
}

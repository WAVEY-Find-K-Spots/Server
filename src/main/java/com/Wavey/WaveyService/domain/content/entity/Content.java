package com.Wavey.WaveyService.domain.content.entity;

import com.Wavey.WaveyService.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contents")
@Getter
@NoArgsConstructor
public class Content extends BaseTimeEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentPlatform platform;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 255)
    private String externalId;

    @Column(nullable = false, length = 500)
    private String thumbnailUrl;

    @Builder
    public Content(ContentPlatform platform, String title, String description,
                   String externalId, String thumbnailUrl) {
        this.platform = platform;
        this.title = title;
        this.description = description;
        this.externalId = externalId;
        this.thumbnailUrl = thumbnailUrl;

    }
    public void update(String title, String description, String externalId, String thumbnailUrl) {
        this.title = title;
        this.description = description;
        this.externalId = externalId;
        this.thumbnailUrl = thumbnailUrl;
    }
}

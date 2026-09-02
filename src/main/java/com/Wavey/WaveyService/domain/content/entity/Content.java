package com.Wavey.WaveyService.domain.content.entity;

import com.Wavey.WaveyService.global.common.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contents")
@AttributeOverride(name = "id", column = @Column(name = "content_id"))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Content extends BaseEntity {

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

    public Long getContentId() {
        return getId();
    }

    public void update(String title, String description, String externalId, String thumbnailUrl) {
        this.title = title;
        this.description = description;
        this.externalId = externalId;
        this.thumbnailUrl = thumbnailUrl;
    }
}

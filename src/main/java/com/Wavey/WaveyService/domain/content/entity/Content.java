package com.Wavey.WaveyService.domain.content.entity;

import com.Wavey.WaveyService.global.common.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contents", uniqueConstraints = @UniqueConstraint(name = "uk_contents_title_category", columnNames = {"title_ko", "category"}))
@AttributeOverride(name = "id", column = @Column(name = "content_id"))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Content extends BaseEntity {

    @Column(name = "title_ko", nullable = false, length = 255)
    private String titleKo;

    @Column(name = "title_en", length = 255)
    private String titleEn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentCategory category;

    public Long getContentId() {
        return getId();
    }

    public String getTitle() { return titleKo; }
    public ContentCategory getCategory() { return category; }
}

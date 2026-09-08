package com.Wavey.WaveyService.domain.work.entity;

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
@Table(
        name = "works",
        uniqueConstraints = @UniqueConstraint(name = "uk_works_title_type", columnNames = {"title", "type"})
)
@AttributeOverride(name = "id", column = @Column(name = "work_id"))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Work extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "title_en", length = 255)
    private String titleEn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkType type;

    @Column(name = "artist_name", length = 255)
    private String artistName;

    public Long getWorkId() {
        return getId();
    }
}

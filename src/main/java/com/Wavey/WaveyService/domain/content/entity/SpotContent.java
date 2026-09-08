package com.Wavey.WaveyService.domain.content.entity;

import com.Wavey.WaveyService.global.common.BaseEntity;

import jakarta.persistence.*;

import lombok.*;

import java.time.*;

@Entity
@Table(name = "spot_contents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotContent extends BaseEntity {

    @Column(nullable = false)
    private Long spotId;

    @Column(nullable = false)
    private Long contentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Kind kind;

    private String titleEn;

    @Column(length = 2000)
    private String descriptionEn;

    private String artist;
    private String episodes;

    @Column(length = 2000)
    private String sceneDescription;

    @Column(length = 2000)
    private String sceneDescriptionEn;

    @Column(length = 1000)
    private String playbackUrl;

    @Column(length = 1000)
    private String sceneUrl;

    private Integer durationSeconds;
    private Integer displayOrder;

    public enum Kind {
        DRAMA,
        MOVIE,
        MUSIC,
        VIDEO
    }
}

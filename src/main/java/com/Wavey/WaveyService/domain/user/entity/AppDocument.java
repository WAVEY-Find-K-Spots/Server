package com.Wavey.WaveyService.domain.user.entity;

import com.Wavey.WaveyService.global.common.BaseEntity;

import jakarta.persistence.*;

import lombok.*;

import java.time.*;

@Entity
@Table(name = "app_documents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppDocument extends BaseEntity {

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private String version;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false)
    private LocalDateTime effectiveAt;
}

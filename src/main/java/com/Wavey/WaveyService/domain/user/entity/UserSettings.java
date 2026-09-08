package com.Wavey.WaveyService.domain.user.entity;

import com.Wavey.WaveyService.global.common.BaseEntity;

import jakarta.persistence.*;

import lombok.*;

import java.time.*;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Long userId;

    @Builder.Default
    @Column(nullable = false)
    private String language = "ko";

    @Builder.Default
    private boolean pushEnabled = true;

    @Builder.Default
    private boolean stampEnabled = true;

    @Builder.Default
    private boolean routeEnabled = true;

    @Builder.Default
    private boolean noticeEnabled = true;

    @Builder.Default
    private boolean locationEnabled = true;
    private boolean marketingEnabled;

    @Column(length = 1000)
    private String profileImageUrl;

    @Column(length = 2)
    private String countryCode;
}

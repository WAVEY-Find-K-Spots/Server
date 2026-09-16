package com.Wavey.WaveyService.domain.user.entity;

import com.Wavey.WaveyService.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSetting extends BaseEntity {

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
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean spotEnabled = true;

    @Builder.Default
    private boolean noticeEnabled = true;

    @Builder.Default
    private boolean locationEnabled = true;

    private boolean marketingEnabled;

    @Column(length = 1000)
    private String profileImageUrl;

    public void updateProfilePreferences(Boolean locationEnabled, Boolean marketingEnabled) {
        if (locationEnabled != null) {
            this.locationEnabled = locationEnabled;
        }
        if (marketingEnabled != null) {
            this.marketingEnabled = marketingEnabled;
        }
    }
}

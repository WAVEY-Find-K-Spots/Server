package com.Wavey.WaveyService.domain.user.entity;

import com.Wavey.WaveyService.domain.user.enums.CountryCode;
import com.Wavey.WaveyService.domain.user.enums.Language;
import com.Wavey.WaveyService.domain.user.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_users_provider_provider_id",
                columnNames = {"provider", "provider_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String providerId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String provider; // "google", "kakao"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 2)
    private CountryCode countryCode;

    @Column(length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(length = 10, nullable = false)
    private Language language = Language.KO;

    @Column(length = 500)
    private String refreshToken;

    public User update(String name, String email) {
        if (name != null && !name.isEmpty()) {
            this.name = name;
        }
        if (email != null && !email.isEmpty()) {
            this.email = email;
        }
        return this;
    }

    public void updateRole(Role role) {
        this.role = role;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void updateCountryCode(CountryCode countryCode) { this.countryCode = countryCode; }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateLanguage(Language language) {
        this.language = language;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

}
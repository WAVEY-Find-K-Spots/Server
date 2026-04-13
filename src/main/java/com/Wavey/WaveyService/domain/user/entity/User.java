package com.Wavey.WaveyService.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String providerId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String provider; // "google", "apple"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

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

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
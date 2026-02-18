package com.Wavey.WaveyService.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users") // 'user'는 DB 예약어일 수 있어 users로 설정
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sub; // 구글 고유 식별값

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String provider; // "google"

    // 유저 정보 업데이트 메소드 (이름 등이 변경되었을 경우 대비)
    public User update(String name, String email) {
        this.name = name;
        this.email = email;
        return this;
    }
}
package com.Wavey.WaveyService.global.config;

import com.Wavey.WaveyService.domain.user.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",                   // 메인 페이지 허용
                                "/swagger-ui/**",      // Swagger 관련 정적 리소스
                                "/swagger-ui.html",    // Swagger HTML 페이지
                                "/v3/api-docs/**",     // Swagger JSON 경로
                                "/api-docs/**",        // 추가 문서 경로
                                "/h2-console/**"       // DB 콘솔
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                // H2 콘솔 사용을 위한 FrameOptions 설정
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                // OAuth2 로그인 설정 추가
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // 커스텀 서비스 연결
                        )
                        .defaultSuccessUrl("/swagger-ui/index.html") // 로그인 성공 후 리다이렉트
                );

        return http.build();
    }
}
package com.Wavey.WaveyService.global.config;

import com.Wavey.WaveyService.service.CustomOAuth2UserService;
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
                // 1. CSRF 비활성화 (개발 환경 및 API 서버)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",                   // 메인 페이지
                                "/swagger-ui/**",      // Swagger UI 리소스
                                "/swagger-ui.html",    // Swagger HTML
                                "/v3/api-docs/**",     // OpenAPI 명세
                                "/api-docs/**",
                                "/h2-console/**",      // H2 데이터베이스 콘솔
                                "/api/**"              // API 경로 (필요에 따라 permitAll 또는 authenticated 설정)
                        ).permitAll()
                        .anyRequest().authenticated()  // 그 외 요청은 인증 필요
                )

                // 3. H2 콘솔 사용을 위한 FrameOptions 설정 (SameOrigin 허용)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                // 4. OAuth2 로그인 설정
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // 커스텀 서비스 연결
                        )
                        .defaultSuccessUrl("/swagger-ui/index.html") // 로그인 성공 후 이동할 페이지
                );

        return http.build();
    }
}
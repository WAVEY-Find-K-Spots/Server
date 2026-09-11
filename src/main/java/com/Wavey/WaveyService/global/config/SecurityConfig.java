package com.Wavey.WaveyService.global.config;

import com.Wavey.WaveyService.domain.user.repository.UserRepository;
import com.Wavey.WaveyService.domain.user.service.CustomOAuth2UserService;
import com.Wavey.WaveyService.domain.user.service.RedisAuthTokenService;
import com.Wavey.WaveyService.global.common.JwtTokenProvider;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import com.Wavey.WaveyService.global.response.ApiResponse;
import com.Wavey.WaveyService.global.response.ErrorDetail;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] DOCUMENTATION_ENDPOINTS = {
            "/v3/api-docs/**",
            "/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**"
    };

    private static final String[] DEVELOPMENT_ENDPOINTS = {
            "/h2-console/**"
    };

    private static final String[] OAUTH_ENDPOINTS = {
            "/oauth2/**",
            "/login/oauth2/**",
            "/api/v1/auth/login-urls",
            "/api/v1/auth/exchange",
            "/api/v1/auth/refresh"
    };

    private static final String[] PUBLIC_ENDPOINTS = {
            "/",
            "/error"
    };

    private static final String VISION_ANALYZE_ENDPOINT = "/api/v1/vision/analyze";
    private static final String PUBLIC_ROUTE_ENDPOINT = "/api/v1/routes/public";

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final RedisAuthTokenService authTokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${auth.allowed-origins:http://localhost:3000}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(DOCUMENTATION_ENDPOINTS).permitAll()
                        .requestMatchers(DEVELOPMENT_ENDPOINTS).permitAll()
                        .requestMatchers(OAUTH_ENDPOINTS).permitAll()
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.POST, VISION_ANALYZE_ENDPOINT).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_ROUTE_ENDPOINT).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            ErrorCode errorCode = (ErrorCode) request.getAttribute("exception");
                            if (errorCode == null) {
                                errorCode = ErrorCode.INVALID_TOKEN;
                            }
                            setErrorResponse(response, errorCode);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            setErrorResponse(response, ErrorCode.ACCESS_DENIED);
                        })
                )
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )
                .addFilterBefore(new JwtAuthenticationFilter(tokenProvider, userRepository, authTokenService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(errorCode.getHttpStatus().value());

        ErrorDetail errorDetail = ErrorDetail.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        ApiResponse<Void> apiResponse = ApiResponse.error(
                errorCode.getHttpStatus().value(),
                errorDetail
        );

        String json = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(json);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

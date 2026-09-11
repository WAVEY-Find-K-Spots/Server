package com.Wavey.WaveyService.global.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.stream.Stream;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;

final class SecurityEndpoints {

    static final String[] DOCUMENTATION = {
            "/v3/api-docs/**",
            "/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**"
    };
    static final String[] DEVELOPMENT = {"/h2-console/**"};
    static final String[] OAUTH = {
            "/oauth2/**",
            "/login/oauth2/**",
            "/api/v1/auth/login-urls",
            "/api/v1/auth/exchange",
            "/api/v1/auth/refresh"
    };
    static final String[] PUBLIC = {"/", "/error"};
    static final String VISION_ANALYZE = "/api/v1/vision/analyze";
    static final String PUBLIC_ROUTE = "/api/v1/routes/public";

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private SecurityEndpoints() {
    }

    static boolean shouldBypassJwtFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        boolean pathIsPublic = Stream.of(DOCUMENTATION, DEVELOPMENT, OAUTH, PUBLIC)
                .flatMap(Arrays::stream)
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
        if (pathIsPublic) {
            return true;
        }
        return (HttpMethod.POST.matches(request.getMethod()) && VISION_ANALYZE.equals(path))
                || (HttpMethod.GET.matches(request.getMethod()) && PUBLIC_ROUTE.equals(path));
    }
}

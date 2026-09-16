package com.Wavey.WaveyService.global.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 로그인 시작 요청(/oauth2/authorization/{provider})에 {@code ?platform=app}이 붙어 있으면
 * 세션에 힌트를 저장해둔다. OAuth2 로그인 성공/실패 후 콜백 시점에는 이 쿼리 파라미터가 없어서
 * ({@link OAuth2SuccessHandler}/{@link OAuth2FailureHandler}) 리다이렉트 목적지를 고를 때
 * 이 힌트를 세션에서 꺼내 참조한다.
 */
public class OAuth2PlatformHintFilter extends OncePerRequestFilter {

    static final String SESSION_ATTRIBUTE = "OAUTH2_PLATFORM_HINT";
    static final String APP_PLATFORM = "app";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/oauth2/authorization/")
                && APP_PLATFORM.equals(request.getParameter("platform"))) {
            HttpSession session = request.getSession(true);
            session.setAttribute(SESSION_ATTRIBUTE, APP_PLATFORM);
        }
        filterChain.doFilter(request, response);
    }
}

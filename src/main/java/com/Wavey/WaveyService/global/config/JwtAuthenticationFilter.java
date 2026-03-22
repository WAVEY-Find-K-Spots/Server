package com.Wavey.WaveyService.global.config;

import com.Wavey.WaveyService.domain.user.repository.UserRepository;
import com.Wavey.WaveyService.global.common.JwtTokenProvider;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null) {
            // validateToken에 request를 전달하여 에러 발생 시 속성에 저장하게 함
            if (tokenProvider.validateToken(token, request)) {
                Claims claims = tokenProvider.getClaims(token);
                String providerId = claims.getSubject();
                String provider = (String) claims.get("provider");

                userRepository.findByProviderAndProviderId(provider, providerId).ifPresent(user -> {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user, null, Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getKey())));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            } else {
                // 토큰 검증 실패 시 에러 응답 직접 전송
                ErrorCode errorCode = (ErrorCode) request.getAttribute("exception");
                if (errorCode != null) {
                    setErrorResponse(response, errorCode);
                    return; // 다음 필터로 진행하지 않고 종료
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(errorCode.getHttpStatus().value());

        String json = String.format(
                "{\"success\":false, \"message\":\"%s\", \"code\":\"%s\"}",
                errorCode.getMessage(), errorCode.getCode()
        );
        response.getWriter().write(json);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/api-docs") ||
                path.startsWith("/api/auth/login-urls") ||
                path.startsWith("/api/auth/refresh") ||
                path.startsWith("/h2-console");
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
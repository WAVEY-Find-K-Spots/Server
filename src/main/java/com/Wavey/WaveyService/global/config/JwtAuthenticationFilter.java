package com.Wavey.WaveyService.global.config;

import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.domain.user.repository.UserRepository;
import com.Wavey.WaveyService.global.common.JwtTokenProvider;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import com.Wavey.WaveyService.global.response.CommonResponse;
import com.Wavey.WaveyService.global.response.ErrorDetail;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Optional;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null) {
            if (tokenProvider.validateToken(token, request)) {
                Claims claims = tokenProvider.getClaims(token);
                String providerId = claims.getSubject();
                String provider = (String) claims.get("provider");

                Optional<User> userOptional = userRepository.findByProviderAndProviderId(provider, providerId);

                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user, null, Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getKey())));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    setErrorResponse(response, ErrorCode.USER_NOT_FOUND);
                    return;
                }
            } else {
                // [피드백 반영] 예기치 못한 검증 실패에 대한 Fallback 로직 추가
                ErrorCode errorCode = (ErrorCode) request.getAttribute("exception");

                // 만약 에러 코드가 설정되어 있지 않다면 기본적으로 INVALID_TOKEN 응답
                if (errorCode == null) {
                    errorCode = ErrorCode.INVALID_TOKEN;
                }

                setErrorResponse(response, errorCode);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(errorCode.getHttpStatus().value());

        ErrorDetail errorDetail = ErrorDetail.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        CommonResponse<Void> commonResponse = CommonResponse.error(
                errorCode.getHttpStatus().value(),
                errorDetail
        );

        String json = objectMapper.writeValueAsString(commonResponse);
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
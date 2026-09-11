package com.Wavey.WaveyService.global.config;

import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.domain.user.repository.UserRepository;
import com.Wavey.WaveyService.domain.user.service.RedisAuthTokenService;
import com.Wavey.WaveyService.global.common.JwtTokenProvider;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.response.ApiResponse;
import com.Wavey.WaveyService.global.response.ErrorDetail;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final RedisAuthTokenService authTokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return SecurityEndpoints.shouldBypassJwtFilter(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null) {
            if (tokenProvider.validateToken(token, request)) {
                Claims claims = tokenProvider.getClaims(token);
                try {
                    if (authTokenService.isAccessTokenBlacklisted(claims.getId())) {
                        setErrorResponse(response, ErrorCode.REVOKED_TOKEN);
                        return;
                    }
                } catch (CustomException e) {
                    setErrorResponse(response, e.getErrorCode());
                    return;
                }
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
                ErrorCode errorCode = (ErrorCode) request.getAttribute("exception");

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

        ApiResponse<Void> apiResponse = ApiResponse.error(
                errorCode.getHttpStatus().value(),
                errorDetail
        );

        String json = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(json);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

package com.Wavey.WaveyService.global.config;

import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.domain.user.repository.UserRepository;
import com.Wavey.WaveyService.domain.user.service.RedisAuthTokenService;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final RedisAuthTokenService authTokenService;
    private final UserRepository userRepository;
    private final String frontendRedirectUri;

    public OAuth2SuccessHandler(
            RedisAuthTokenService authTokenService,
            UserRepository userRepository,
            @Value("${auth.frontend-redirect-uri:http://localhost:3000/oauth/callback}") String frontendRedirectUri
    ) {
        this.authTokenService = authTokenService;
        this.userRepository = userRepository;
        this.frontendRedirectUri = frontendRedirectUri;
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String providerId = oAuth2User.getAttribute("providerId");
        String provider = oAuth2User.getAttribute("provider");

        if (providerId == null || provider == null) {
            throw new IllegalStateException("Missing required OAuth2 attributes: providerId or provider");
        }

        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String loginCode = authTokenService.issueLoginCode(user.getId());

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("code", loginCode)
                .build()
                .encode()
                .toUriString();

        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}

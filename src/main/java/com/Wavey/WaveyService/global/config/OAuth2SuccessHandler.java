package com.Wavey.WaveyService.global.config;

import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.domain.user.repository.UserRepository;
import com.Wavey.WaveyService.domain.user.service.RedisAuthTokenService;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
    private final String appRedirectUri;

    public OAuth2SuccessHandler(
            RedisAuthTokenService authTokenService,
            UserRepository userRepository,
            @Value("${auth.frontend-redirect-uri:http://localhost:3000/oauth/callback}") String frontendRedirectUri,
            @Value("${auth.app-redirect-uri:wavey://oauth/callback}") String appRedirectUri
    ) {
        this.authTokenService = authTokenService;
        this.userRepository = userRepository;
        this.frontendRedirectUri = frontendRedirectUri;
        this.appRedirectUri = appRedirectUri;
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String redirectUrl;
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            String providerId = oAuth2User.getAttribute("providerId");
            String provider = oAuth2User.getAttribute("provider");

            if (providerId == null || provider == null) {
                throw new CustomException(ErrorCode.OAUTH_INVALID_USER_INFO);
            }

            User user = userRepository.findByProviderAndProviderId(provider, providerId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            boolean isNewUser = Boolean.TRUE.equals(oAuth2User.getAttribute("isNewUser"));
            String loginCode = authTokenService.issueLoginCode(user.getId(), isNewUser);

            redirectUrl = buildRedirectUrl(request, "code", loginCode);
        } catch (CustomException e) {
            redirectUrl = buildRedirectUrl(request, "error", e.getErrorCode().getCode());
        }

        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String buildRedirectUrl(HttpServletRequest request, String parameter, String value) {
        return UriComponentsBuilder.fromUriString(resolveRedirectBaseUri(request))
                .queryParam(parameter, value)
                .build()
                .encode()
                .toUriString();
    }

    private String resolveRedirectBaseUri(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null
                && OAuth2PlatformHintFilter.APP_PLATFORM.equals(
                        session.getAttribute(OAuth2PlatformHintFilter.SESSION_ATTRIBUTE))) {
            session.removeAttribute(OAuth2PlatformHintFilter.SESSION_ATTRIBUTE);
            return appRedirectUri;
        }
        return frontendRedirectUri;
    }
}

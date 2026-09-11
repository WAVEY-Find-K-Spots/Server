package com.Wavey.WaveyService.global.config;

import com.Wavey.WaveyService.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final String GENERIC_ERROR = "oauth2_authentication_failed";
    private static final Set<String> CLIENT_ACTIONABLE_ERRORS = Set.of(
            ErrorCode.OAUTH_INVALID_USER_INFO.getCode(),
            ErrorCode.OAUTH_EMAIL_REQUIRED.getCode(),
            ErrorCode.OAUTH_PROFILE_REQUIRED.getCode()
    );

    private final String frontendRedirectUri;

    public OAuth2FailureHandler(
            @Value("${auth.frontend-redirect-uri:http://localhost:3000/oauth/callback}")
            String frontendRedirectUri
    ) {
        this.frontendRedirectUri = frontendRedirectUri;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("error", resolveClientError(exception))
                .build()
                .encode()
                .toUriString();

        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String resolveClientError(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauthException) {
            String errorCode = oauthException.getError().getErrorCode();
            if (CLIENT_ACTIONABLE_ERRORS.contains(errorCode)) {
                return errorCode;
            }
        }
        return GENERIC_ERROR;
    }
}

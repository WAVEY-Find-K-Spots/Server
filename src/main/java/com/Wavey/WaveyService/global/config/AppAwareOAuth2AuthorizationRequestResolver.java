package com.Wavey.WaveyService.global.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * 로그인 시작 요청(/oauth2/authorization/{provider})에 {@code ?platform=app}이 붙어 있으면
 * 이를 OAuth2 {@code state} 값에 인코딩해둔다. {@code state}는 세션 저장소 구현체와 무관하게
 * 인가 요청과 콜백 사이를 항상 왕복하는 값이므로, STATELESS 세션 정책 하에서도
 * ({@link OAuth2SuccessHandler}/{@link OAuth2FailureHandler}) 콜백 시점에 안정적으로 힌트를 복원할 수 있다.
 */
public class AppAwareOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    static final String APP_STATE_PREFIX = "app:";
    private static final String PLATFORM_PARAM = "platform";
    private static final String APP_PLATFORM = "app";

    private final OAuth2AuthorizationRequestResolver delegate;

    public AppAwareOAuth2AuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository,
            String authorizationRequestBaseUri
    ) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, authorizationRequestBaseUri);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return withAppStateIfRequested(delegate.resolve(request), request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return withAppStateIfRequested(delegate.resolve(request, clientRegistrationId), request);
    }

    private OAuth2AuthorizationRequest withAppStateIfRequested(
            OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request
    ) {
        if (authorizationRequest == null || !APP_PLATFORM.equals(request.getParameter(PLATFORM_PARAM))) {
            return authorizationRequest;
        }
        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .state(APP_STATE_PREFIX + authorizationRequest.getState())
                .build();
    }

    static boolean isAppState(String state) {
        return state != null && state.startsWith(APP_STATE_PREFIX);
    }
}

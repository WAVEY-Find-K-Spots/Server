package com.Wavey.WaveyService.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

class OAuth2FailureHandlerTest {

    @Test
    void 로그인_실패시_내부_예외를_노출하지_않고_프론트로_리다이렉트한다() throws Exception {
        OAuth2FailureHandler handler = new OAuth2FailureHandler(
                "http://localhost:3000/oauth/callback"
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException exception = new AuthenticationException("민감한 내부 오류") { };

        handler.onAuthenticationFailure(request, response, exception);

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getRedirectedUrl()).isEqualTo(
                "http://localhost:3000/oauth/callback?error=oauth2_authentication_failed"
        );
        assertThat(response.getHeader(HttpHeaders.CACHE_CONTROL)).isEqualTo("no-store");
        assertThat(response.getHeader(HttpHeaders.PRAGMA)).isEqualTo("no-cache");
        assertThat(response.getRedirectedUrl()).doesNotContain("민감한 내부 오류");
    }
}

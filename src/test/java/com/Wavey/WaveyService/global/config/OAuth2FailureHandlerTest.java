package com.Wavey.WaveyService.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.Wavey.WaveyService.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

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

    @Test
    void 이메일_동의_누락은_프론트가_처리할_수_있는_오류코드로_전달한다() throws Exception {
        OAuth2FailureHandler handler = new OAuth2FailureHandler(
                "http://localhost:3000/oauth/callback"
        );
        MockHttpServletResponse response = new MockHttpServletResponse();
        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(
                new OAuth2Error(ErrorCode.OAUTH_EMAIL_REQUIRED.getCode())
        );

        handler.onAuthenticationFailure(new MockHttpServletRequest(), response, exception);

        assertThat(response.getRedirectedUrl()).isEqualTo(
                "http://localhost:3000/oauth/callback?error=OAUTH_400_EMAIL_REQUIRED"
        );
    }
}

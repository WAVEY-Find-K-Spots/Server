package com.Wavey.WaveyService.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class SecurityEndpointsTest {

    @Test
    void refresh_요청은_오래된_Authorization_헤더가_있어도_JWT_필터를_건너뛴다() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/refresh");
        request.setRequestURI("/api/v1/auth/refresh");
        request.addHeader("Authorization", "Bearer expired-access-token");

        assertThat(SecurityEndpoints.shouldBypassJwtFilter(request)).isTrue();
    }

    @Test
    void 로컬_회원가입과_로그인은_JWT_필터를_건너뛴다() {
        MockHttpServletRequest signup =
                new MockHttpServletRequest("POST", "/api/v1/auth/local/signup");
        signup.setRequestURI("/api/v1/auth/local/signup");
        MockHttpServletRequest login =
                new MockHttpServletRequest("POST", "/api/v1/auth/local/login");
        login.setRequestURI("/api/v1/auth/local/login");

        assertThat(SecurityEndpoints.shouldBypassJwtFilter(signup)).isTrue();
        assertThat(SecurityEndpoints.shouldBypassJwtFilter(login)).isTrue();
    }

    @Test
    void 비밀번호_재설정_요청과_확정은_JWT_필터를_건너뛴다() {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST", "/api/v1/auth/local/password/reset/request"
        );
        request.setRequestURI("/api/v1/auth/local/password/reset/request");
        MockHttpServletRequest confirm = new MockHttpServletRequest(
                "POST", "/api/v1/auth/local/password/reset/confirm"
        );
        confirm.setRequestURI("/api/v1/auth/local/password/reset/confirm");

        assertThat(SecurityEndpoints.shouldBypassJwtFilter(request)).isTrue();
        assertThat(SecurityEndpoints.shouldBypassJwtFilter(confirm)).isTrue();
    }
    @Test
    void 보호된_요청은_JWT_필터를_통과한다() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/routes");
        request.setRequestURI("/api/v1/routes");

        assertThat(SecurityEndpoints.shouldBypassJwtFilter(request)).isFalse();
    }

    @Test
    void 정책_조회는_로그인_없이_JWT_필터를_건너뛴다() {
        MockHttpServletRequest policyRequest = new MockHttpServletRequest("GET", "/api/v1/policies");
        policyRequest.setRequestURI("/api/v1/policies");

        assertThat(SecurityEndpoints.shouldBypassJwtFilter(policyRequest)).isTrue();
    }

    @Test
    void 제거된_기존_정책_엔드포인트는_공개_정책으로_처리하지_않는다() {
        MockHttpServletRequest oldTermsRequest = new MockHttpServletRequest("GET", "/api/v1/policies/terms");
        oldTermsRequest.setRequestURI("/api/v1/policies/terms");

        assertThat(SecurityEndpoints.shouldBypassJwtFilter(oldTermsRequest)).isFalse();
    }
}

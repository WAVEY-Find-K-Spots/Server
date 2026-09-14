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
    void 보호된_요청은_JWT_필터를_통과한다() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/routes");
        request.setRequestURI("/api/v1/routes");

        assertThat(SecurityEndpoints.shouldBypassJwtFilter(request)).isFalse();
    }
}

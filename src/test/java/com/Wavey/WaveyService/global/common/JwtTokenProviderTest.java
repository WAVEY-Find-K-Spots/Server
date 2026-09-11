package com.Wavey.WaveyService.global.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "secretKey",
                "dGVzdC1qd3Qtc2VjcmV0LXRlc3Qtand0LXNlY3JldC10ZXN0LWp3dC1zZWNyZXQ=");
        ReflectionTestUtils.setField(tokenProvider, "accessTokenExpiration", 3_600_000L);
        ReflectionTestUtils.setField(tokenProvider, "refreshTokenExpiration", 1_209_600_000L);
        tokenProvider.init();
    }

    @Test
    void access와_refresh_토큰의_용도를_구분한다() {
        String accessToken = tokenProvider.createAccessToken("google", "provider-id");
        String refreshToken = tokenProvider.createRefreshToken("google", "provider-id");

        Claims accessClaims = tokenProvider.getValidatedClaims(accessToken, TokenType.ACCESS);
        Claims refreshClaims = tokenProvider.getValidatedClaims(refreshToken, TokenType.REFRESH);

        assertThat(accessClaims.getId()).isNotBlank();
        assertThat(refreshClaims.getId()).isNotBlank();
        assertThat(accessClaims.get("tokenType", String.class)).isEqualTo("ACCESS");
        assertThat(refreshClaims.get("tokenType", String.class)).isEqualTo("REFRESH");
        assertThatThrownBy(() -> tokenProvider.getValidatedClaims(refreshToken, TokenType.ACCESS))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_TOKEN);
    }
}

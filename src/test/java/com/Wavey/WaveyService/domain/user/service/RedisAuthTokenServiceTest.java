package com.Wavey.WaveyService.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RedisAuthTokenServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisAuthTokenService authTokenService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        authTokenService = new RedisAuthTokenService(redisTemplate);
        ReflectionTestUtils.setField(authTokenService, "loginCodeTtl", Duration.ofMinutes(3));
    }

    @Test
    void 로그인_코드는_해시된_키와_TTL로_저장한다() {
        String code = authTokenService.issueLoginCode(7L);

        assertThat(code).isNotBlank();
        verify(valueOperations).set(
                argThat(key -> key.startsWith("auth:login-code:") && !key.contains(code)),
                eq("7"),
                eq(Duration.ofMinutes(3))
        );
    }

    @Test
    void 로그인_코드는_조회와_동시에_삭제한다() {
        when(valueOperations.getAndDelete(anyString())).thenReturn("7");

        assertThat(authTokenService.consumeLoginCode("one-time-code")).isEqualTo(7L);
        verify(valueOperations).getAndDelete(anyString());
    }

    @Test
    void refresh_토큰은_해시된_값과_TTL로_저장한다() throws Exception {
        String rawToken = "refresh-token";
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(rawToken.getBytes(StandardCharsets.UTF_8)));

        authTokenService.saveRefreshToken(7L, rawToken, Duration.ofMinutes(10));

        verify(valueOperations).set(
                "auth:refresh:7",
                hash,
                Duration.ofMinutes(10)
        );
    }

    @Test
    void Redis_연결_실패는_인증_저장소_오류로_변환한다() {
        when(valueOperations.getAndDelete(anyString()))
                .thenThrow(new RedisConnectionFailureException("down"));

        assertThatThrownBy(() -> authTokenService.consumeLoginCode("code"))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_STORAGE_UNAVAILABLE);
    }
}

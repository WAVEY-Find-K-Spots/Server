package com.Wavey.WaveyService.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
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
        authTokenService = new RedisAuthTokenService(redisTemplate);
        ReflectionTestUtils.setField(authTokenService, "loginCodeTtl", Duration.ofMinutes(3));
    }

    @Test
    void 로그인_코드는_해시된_키와_TTL로_저장한다() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String code = authTokenService.issueLoginCode(7L);

        assertThat(code).isNotBlank();
        verify(valueOperations).set(
                org.mockito.ArgumentMatchers.argThat(key -> key.startsWith("auth:login-code:") && !key.contains(code)),
                eq("7"),
                eq(Duration.ofMinutes(3))
        );
    }

    @Test
    void 로그인_코드에서_사용자_ID를_조회한다() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("7");

        assertThat(authTokenService.getLoginCodeUserId("one-time-code")).isEqualTo(7L);
        verify(valueOperations).get(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void 로그인_코드_교환과_refresh_저장을_원자적으로_처리한다() {
        when(redisTemplate.execute(
                any(DefaultRedisScript.class), anyList(), any(), any(), any()
        )).thenReturn(1L);

        boolean exchanged = authTokenService.exchangeLoginCode(
                "one-time-code", 7L, "refresh-token", Duration.ofMinutes(10)
        );

        assertThat(exchanged).isTrue();
        verify(redisTemplate).execute(
                any(DefaultRedisScript.class), anyList(), eq("7"), anyString(), eq("600000")
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void refresh_토큰을_검증하고_새_토큰으로_원자적_교체한다() {
        when(redisTemplate.execute(
                any(DefaultRedisScript.class), anyList(), any(), any(), any()
        )).thenReturn(1L);

        boolean rotated = authTokenService.rotateRefreshToken(
                7L, "current-refresh", "replacement-refresh", Duration.ofMinutes(10)
        );

        assertThat(rotated).isTrue();
    }

    @Test
    void Redis_연결_실패는_인증_저장소_오류로_변환한다() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString()))
                .thenThrow(new RedisConnectionFailureException("down"));

        assertThatThrownBy(() -> authTokenService.getLoginCodeUserId("code"))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_STORAGE_UNAVAILABLE);
    }
}

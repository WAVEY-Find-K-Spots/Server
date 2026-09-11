package com.Wavey.WaveyService.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.Wavey.WaveyService.global.exception.CustomException;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@EnabledIfEnvironmentVariable(named = "RUN_REDIS_INTEGRATION_TEST", matches = "true")
class RedisAuthTokenIntegrationTest {

    private LettuceConnectionFactory connectionFactory;
    private RedisAuthTokenService authTokenService;

    @BeforeEach
    void setUp() {
        connectionFactory = new LettuceConnectionFactory("127.0.0.1", 6379);
        connectionFactory.afterPropertiesSet();
        connectionFactory.start();

        StringRedisTemplate redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
        authTokenService = new RedisAuthTokenService(redisTemplate);
        ReflectionTestUtils.setField(authTokenService, "loginCodeTtl", Duration.ofMinutes(3));
    }

    @AfterEach
    void tearDown() {
        connectionFactory.destroy();
    }

    @Test
    void 실제_Redis에서_로그인코드_교환과_refresh_회전을_원자적으로_처리한다() {
        String loginCode = authTokenService.issueLoginCode(99L);
        assertThat(authTokenService.getLoginCodeUserId(loginCode)).isEqualTo(99L);

        String refreshToken = "refresh-" + UUID.randomUUID();
        assertThat(authTokenService.exchangeLoginCode(
                loginCode, 99L, refreshToken, Duration.ofMinutes(1)
        )).isTrue();
        assertThatThrownBy(() -> authTokenService.getLoginCodeUserId(loginCode))
                .isInstanceOf(CustomException.class);

        String replacementToken = "refresh-" + UUID.randomUUID();
        assertThat(authTokenService.rotateRefreshToken(
                99L, refreshToken, replacementToken, Duration.ofMinutes(1)
        )).isTrue();
        assertThat(authTokenService.rotateRefreshToken(
                99L, refreshToken, "replay", Duration.ofMinutes(1)
        )).isFalse();
        authTokenService.deleteRefreshToken(99L);
    }
}

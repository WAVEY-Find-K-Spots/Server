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
    void 실제_Redis에서_로그인코드와_refresh_토큰을_한번만_소비한다() {
        String loginCode = authTokenService.issueLoginCode(99L);
        assertThat(authTokenService.consumeLoginCode(loginCode)).isEqualTo(99L);
        assertThatThrownBy(() -> authTokenService.consumeLoginCode(loginCode))
                .isInstanceOf(CustomException.class);

        String refreshToken = "refresh-" + UUID.randomUUID();
        authTokenService.saveRefreshToken(99L, refreshToken, Duration.ofMinutes(1));
        assertThat(authTokenService.consumeRefreshToken(99L, refreshToken)).isTrue();
        assertThat(authTokenService.consumeRefreshToken(99L, refreshToken)).isFalse();
    }
}

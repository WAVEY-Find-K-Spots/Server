package com.Wavey.WaveyService.domain.user.service;

import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RedisAuthTokenService {

    private static final String LOGIN_CODE_PREFIX = "auth:login-code:";
    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";
    private static final String ACCESS_BLACKLIST_PREFIX = "auth:blacklist:access:";
    private static final DefaultRedisScript<Long> CONSUME_REFRESH_TOKEN_SCRIPT =
            new DefaultRedisScript<>(
                    "if redis.call('get', KEYS[1]) == ARGV[1] "
                            + "then return redis.call('del', KEYS[1]) else return 0 end",
                    Long.class
            );

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${auth.login-code-ttl:3m}")
    private Duration loginCodeTtl;

    public String issueLoginCode(Long userId) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String rawCode = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        execute(() -> {
            redisTemplate.opsForValue().set(loginCodeKey(rawCode), userId.toString(), loginCodeTtl);
            return null;
        });
        return rawCode;
    }

    public Long consumeLoginCode(String rawCode) {
        if (!StringUtils.hasText(rawCode)) {
            throw new CustomException(ErrorCode.INVALID_LOGIN_CODE);
        }

        String userId = execute(() -> redisTemplate.opsForValue().getAndDelete(loginCodeKey(rawCode)));
        if (!StringUtils.hasText(userId)) {
            throw new CustomException(ErrorCode.INVALID_LOGIN_CODE);
        }

        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_LOGIN_CODE);
        }
    }

    public void saveRefreshToken(Long userId, String rawToken, Duration ttl) {
        if (ttl.isZero() || ttl.isNegative()) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        }
        execute(() -> {
            redisTemplate.opsForValue().set(refreshTokenKey(userId), hash(rawToken), ttl);
            return null;
        });
    }

    public boolean consumeRefreshToken(Long userId, String rawToken) {
        Long result = execute(() -> redisTemplate.execute(
                CONSUME_REFRESH_TOKEN_SCRIPT,
                List.of(refreshTokenKey(userId)),
                hash(rawToken)
        ));
        return Long.valueOf(1L).equals(result);
    }

    public void deleteRefreshToken(Long userId) {
        execute(() -> redisTemplate.delete(refreshTokenKey(userId)));
    }

    public void blacklistAccessToken(String tokenId, Duration ttl) {
        if (!StringUtils.hasText(tokenId) || ttl.isZero() || ttl.isNegative()) {
            return;
        }
        execute(() -> {
            redisTemplate.opsForValue().set(ACCESS_BLACKLIST_PREFIX + tokenId, "logout", ttl);
            return null;
        });
    }

    public boolean isAccessTokenBlacklisted(String tokenId) {
        if (!StringUtils.hasText(tokenId)) {
            return false;
        }
        return Boolean.TRUE.equals(execute(() -> redisTemplate.hasKey(ACCESS_BLACKLIST_PREFIX + tokenId)));
    }

    private String loginCodeKey(String rawCode) {
        return LOGIN_CODE_PREFIX + hash(rawCode);
    }

    private String refreshTokenKey(Long userId) {
        return REFRESH_TOKEN_PREFIX + userId;
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private <T> T execute(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataAccessException e) {
            throw new CustomException(ErrorCode.AUTH_STORAGE_UNAVAILABLE);
        }
    }
}

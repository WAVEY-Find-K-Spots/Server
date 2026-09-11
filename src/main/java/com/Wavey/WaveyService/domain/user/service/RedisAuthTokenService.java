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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisAuthTokenService {

    private static final String LOGIN_CODE_PREFIX = "auth:login-code:";
    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";
    private static final String ACCESS_BLACKLIST_PREFIX = "auth:blacklist:access:";
    private static final DefaultRedisScript<Long> ROTATE_REFRESH_TOKEN_SCRIPT =
            new DefaultRedisScript<>(
                    "if redis.call('get', KEYS[1]) == ARGV[1] "
                            + "then redis.call('set', KEYS[1], ARGV[2], 'PX', ARGV[3]); "
                            + "return 1 else return 0 end",
                    Long.class
            );
    private static final DefaultRedisScript<Long> EXCHANGE_LOGIN_CODE_SCRIPT =
            new DefaultRedisScript<>(
                    "if redis.call('get', KEYS[1]) == ARGV[1] "
                            + "then redis.call('set', KEYS[2], ARGV[2], 'PX', ARGV[3]); "
                            + "redis.call('del', KEYS[1]); return 1 else return 0 end",
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

        execute("issue login code", () -> {
            redisTemplate.opsForValue().set(loginCodeKey(rawCode), userId.toString(), loginCodeTtl);
            return null;
        });
        return rawCode;
    }

    public Long getLoginCodeUserId(String rawCode) {
        if (!StringUtils.hasText(rawCode)) {
            throw new CustomException(ErrorCode.INVALID_LOGIN_CODE);
        }

        String userId = execute("read login code", () -> redisTemplate.opsForValue().get(loginCodeKey(rawCode)));
        if (!StringUtils.hasText(userId)) {
            throw new CustomException(ErrorCode.INVALID_LOGIN_CODE);
        }

        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_LOGIN_CODE);
        }
    }

    public boolean exchangeLoginCode(String rawCode, Long userId, String refreshToken, Duration ttl) {
        validateTtl(ttl);
        Long result = execute("exchange login code", () -> redisTemplate.execute(
                EXCHANGE_LOGIN_CODE_SCRIPT,
                List.of(loginCodeKey(rawCode), refreshTokenKey(userId)),
                userId.toString(),
                hash(refreshToken),
                Long.toString(ttl.toMillis())
        ));
        return Long.valueOf(1L).equals(result);
    }

    public boolean rotateRefreshToken(
            Long userId,
            String currentRefreshToken,
            String replacementRefreshToken,
            Duration ttl
    ) {
        validateTtl(ttl);
        Long result = execute("rotate refresh token", () -> redisTemplate.execute(
                ROTATE_REFRESH_TOKEN_SCRIPT,
                List.of(refreshTokenKey(userId)),
                hash(currentRefreshToken),
                hash(replacementRefreshToken),
                Long.toString(ttl.toMillis())
        ));
        return Long.valueOf(1L).equals(result);
    }

    public void deleteRefreshToken(Long userId) {
        execute("delete refresh token", () -> redisTemplate.delete(refreshTokenKey(userId)));
    }

    public void blacklistAccessToken(String tokenId, Duration ttl) {
        if (!StringUtils.hasText(tokenId) || ttl.isZero() || ttl.isNegative()) {
            return;
        }
        execute("blacklist access token", () -> {
            redisTemplate.opsForValue().set(ACCESS_BLACKLIST_PREFIX + tokenId, "logout", ttl);
            return null;
        });
    }

    public boolean isAccessTokenBlacklisted(String tokenId) {
        if (!StringUtils.hasText(tokenId)) {
            return false;
        }
        return Boolean.TRUE.equals(execute(
                "check access token blacklist",
                () -> redisTemplate.hasKey(ACCESS_BLACKLIST_PREFIX + tokenId)
        ));
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

    private void validateTtl(Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        }
    }

    /**
     * 인증 저장소 장애 시 인증을 허용하지 않는 fail-closed 정책을 적용한다.
     * 로그에는 토큰이나 로그인 코드를 남기지 않고 실패한 작업 종류만 기록한다.
     */
    private <T> T execute(String operationName, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataAccessException e) {
            log.error(
                    "Authentication storage operation failed: {} ({})",
                    operationName,
                    e.getClass().getSimpleName()
            );
            throw new CustomException(ErrorCode.AUTH_STORAGE_UNAVAILABLE);
        }
    }
}

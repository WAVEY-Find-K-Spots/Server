package com.Wavey.WaveyService.domain.docent.service;

import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** 사용자 또는 IP별 이미지 분석 요청 횟수를 고정 시간 창 단위로 제한합니다. */
@Component
public class VisionRequestRateLimiter {

    private final boolean enabled;
    private final int maxRequests;
    private final long windowMillis;
    private final Clock clock;
    private final ConcurrentHashMap<String, RequestWindow> windows = new ConcurrentHashMap<>();

    @Autowired
    public VisionRequestRateLimiter(
            @Value("${vision.rate-limit.enabled:true}") boolean enabled,
            @Value("${vision.rate-limit.max-requests:30}") int maxRequests,
            @Value("${vision.rate-limit.window-seconds:60}") long windowSeconds
    ) {
        this(enabled, maxRequests, windowSeconds, Clock.systemUTC());
    }

    VisionRequestRateLimiter(boolean enabled, int maxRequests, long windowSeconds, Clock clock) {
        if (maxRequests < 1 || windowSeconds < 1) {
            throw new IllegalArgumentException("Vision rate limit values must be positive");
        }
        this.enabled = enabled;
        this.maxRequests = maxRequests;
        this.windowMillis = Math.multiplyExact(windowSeconds, 1_000L);
        this.clock = clock;
    }

    public void check(User user, String remoteAddress) {
        if (!enabled) {
            return;
        }

        String key = requestKey(user, remoteAddress);
        long now = clock.millis();
        AtomicBoolean exceeded = new AtomicBoolean(false);
        windows.compute(key, (ignored, current) -> {
            if (current == null || now - current.startedAtMillis() >= windowMillis) {
                return new RequestWindow(now, 1);
            }
            if (current.requestCount() >= maxRequests) {
                exceeded.set(true);
                return current;
            }
            return new RequestWindow(current.startedAtMillis(), current.requestCount() + 1);
        });

        if (exceeded.get()) {
            throw new CustomException(ErrorCode.VISION_RATE_LIMIT_EXCEEDED);
        }
    }

    private String requestKey(User user, String remoteAddress) {
        if (user != null && user.getId() != null) {
            return "user:" + user.getId();
        }
        String address = remoteAddress == null || remoteAddress.isBlank()
                ? "unknown"
                : remoteAddress.strip();
        return "ip:" + address;
    }

    private record RequestWindow(long startedAtMillis, int requestCount) {
    }
}

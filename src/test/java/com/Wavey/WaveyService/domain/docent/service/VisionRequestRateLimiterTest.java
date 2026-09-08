package com.Wavey.WaveyService.domain.docent.service;

import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class VisionRequestRateLimiterTest {

    @Test
    void 같은_사용자가_한도를_초과하면_429_오류를_반환한다() {
        VisionRequestRateLimiter limiter = new VisionRequestRateLimiter(
                true,
                1,
                60,
                Clock.fixed(Instant.parse("2026-09-08T00:00:00Z"), ZoneOffset.UTC)
        );
        User user = User.builder().id(1L).build();

        limiter.check(user, "127.0.0.1");
        CustomException exception = catchThrowableOfType(
                () -> limiter.check(user, "127.0.0.1"),
                CustomException.class
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VISION_RATE_LIMIT_EXCEEDED);
    }

    @Test
    void 인증_사용자가_다르면_같은_IP에서도_각자_한도를_사용한다() {
        VisionRequestRateLimiter limiter = new VisionRequestRateLimiter(
                true,
                1,
                60,
                Clock.fixed(Instant.parse("2026-09-08T00:00:00Z"), ZoneOffset.UTC)
        );

        limiter.check(User.builder().id(1L).build(), "127.0.0.1");
        limiter.check(User.builder().id(2L).build(), "127.0.0.1");
    }
}

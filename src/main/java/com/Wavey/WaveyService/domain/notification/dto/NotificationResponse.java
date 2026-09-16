package com.Wavey.WaveyService.domain.notification.dto;

import com.Wavey.WaveyService.domain.notification.enums.NotificationTargetType;
import com.Wavey.WaveyService.domain.notification.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "알림 항목")
public record NotificationResponse(
        @Schema(example = "42") Long id,
        @Schema(example = "stamp") NotificationType type,
        @Schema(example = "새 스탬프를 획득했어요") String title,
        @Schema(example = "경복궁 스탬프가 스탬프북에 추가되었습니다.") String body,
        @Schema(example = "stamp") NotificationTargetType targetType,
        @Schema(example = "7") Long targetId,
        @Schema(example = "false") boolean read,
        @Schema(example = "2026-09-16T14:00:00", nullable = true) LocalDateTime readAt,
        @Schema(example = "2026-09-16T13:50:00") LocalDateTime createdAt) {}

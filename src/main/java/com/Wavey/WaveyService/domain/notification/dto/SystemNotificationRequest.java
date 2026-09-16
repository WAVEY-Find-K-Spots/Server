package com.Wavey.WaveyService.domain.notification.dto;

import com.Wavey.WaveyService.domain.notification.enums.NotificationTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

@Schema(description = "관리자 시스템 알림 생성 요청")
public record SystemNotificationRequest(
        @NotBlank @Size(max = 150) String title,
        @Size(max = 150) String titleEn,
        @NotBlank @Size(max = 500) String body,
        @Size(max = 500) String bodyEn,
        @Schema(description = "비우면 전체 사용자에게 발송") Set<Long> recipientUserIds,
        NotificationTargetType targetType,
        Long targetId,
        @NotBlank @Size(max = 120) String eventKey) {}

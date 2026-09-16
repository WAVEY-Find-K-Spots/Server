package com.Wavey.WaveyService.domain.notification.dto;

import com.Wavey.WaveyService.domain.notification.enums.NotificationTargetType;
import com.Wavey.WaveyService.domain.notification.enums.NotificationType;

public record NotificationCreateCommand(
        Long userId,
        NotificationType type,
        String titleKo,
        String titleEn,
        String bodyKo,
        String bodyEn,
        NotificationTargetType targetType,
        Long targetId,
        String eventKey) {}

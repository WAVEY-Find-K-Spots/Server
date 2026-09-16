package com.Wavey.WaveyService.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 수신 설정")
public record NotificationSettingsResponse(
        boolean pushEnabled,
        boolean stampEnabled,
        boolean routeEnabled,
        boolean spotEnabled,
        boolean noticeEnabled) {}

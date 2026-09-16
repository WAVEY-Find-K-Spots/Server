package com.Wavey.WaveyService.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 수신 설정 변경. 전달한 필드만 변경됩니다.")
public record NotificationSettingsUpdateRequest(
        Boolean pushEnabled,
        Boolean stampEnabled,
        Boolean routeEnabled,
        Boolean spotEnabled,
        Boolean noticeEnabled) {}

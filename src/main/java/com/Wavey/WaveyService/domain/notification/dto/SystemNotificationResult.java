package com.Wavey.WaveyService.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "시스템 알림 생성 결과")
public record SystemNotificationResult(int requestedCount, int createdCount, int skippedCount) {}

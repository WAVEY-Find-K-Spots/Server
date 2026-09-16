package com.Wavey.WaveyService.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "내 알림함")
public record NotificationInboxResponse(
        @Schema(example = "3") long unreadCount,
        List<NotificationResponse> notifications,
        @Schema(example = "0") int page,
        @Schema(example = "20") int size,
        @Schema(example = "23") long totalElements,
        @Schema(example = "2") int totalPages,
        @Schema(example = "true") boolean hasNext) {}

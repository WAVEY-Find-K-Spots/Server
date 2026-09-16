package com.Wavey.WaveyService.domain.notification.controller;

import com.Wavey.WaveyService.domain.notification.dto.SystemNotificationRequest;
import com.Wavey.WaveyService.domain.notification.dto.SystemNotificationResult;
import com.Wavey.WaveyService.domain.notification.service.NotificationService;
import com.Wavey.WaveyService.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Notification", description = "관리자 시스템 알림 API")
@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "전체 또는 선택 사용자에게 시스템 알림 생성")
    @PostMapping("/system")
    public ResponseEntity<CommonResponse<SystemNotificationResult>> createSystemNotification(
            @Valid @RequestBody SystemNotificationRequest request) {
        SystemNotificationResult result =
                notificationService.createSystemNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(201, "시스템 알림 생성 성공", result));
    }
}

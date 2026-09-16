package com.Wavey.WaveyService.domain.notification.controller;

import com.Wavey.WaveyService.domain.notification.dto.NotificationInboxResponse;
import com.Wavey.WaveyService.domain.notification.dto.NotificationResponse;
import com.Wavey.WaveyService.domain.notification.dto.NotificationSettingsResponse;
import com.Wavey.WaveyService.domain.notification.dto.NotificationSettingsUpdateRequest;
import com.Wavey.WaveyService.domain.notification.service.NotificationService;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notification", description = "내 알림함 및 알림 수신 설정 API")
@RestController
@RequestMapping("/api/v1/me/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "내 알림 목록 조회")
    @GetMapping
    public ResponseEntity<CommonResponse<NotificationInboxResponse>> inbox(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "ko") String language,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return ResponseEntity.ok(
                CommonResponse.success(
                        "알림 목록 조회 성공",
                        notificationService.inbox(user.getId(), language, page, size)));
    }

    @Operation(summary = "알림 읽음 처리")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<CommonResponse<NotificationResponse>> read(
            @AuthenticationPrincipal User user,
            @PathVariable Long notificationId,
            @RequestParam(defaultValue = "ko") String language) {
        return ResponseEntity.ok(
                CommonResponse.success(
                        "알림 읽음 처리 성공",
                        notificationService.read(user.getId(), notificationId, language)));
    }

    @Operation(summary = "모든 알림 읽음 처리")
    @PatchMapping("/read-all")
    public ResponseEntity<CommonResponse<Integer>> readAll(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                CommonResponse.success(
                        "모든 알림 읽음 처리 성공",
                        notificationService.readAll(user.getId())));
    }

    @Operation(summary = "알림 수신 설정 조회")
    @GetMapping("/settings")
    public ResponseEntity<CommonResponse<NotificationSettingsResponse>> settings(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                CommonResponse.success(
                        "알림 설정 조회 성공",
                        notificationService.getSettings(user.getId())));
    }

    @Operation(summary = "알림 수신 설정 변경")
    @PatchMapping("/settings")
    public ResponseEntity<CommonResponse<NotificationSettingsResponse>> updateSettings(
            @AuthenticationPrincipal User user,
            @RequestBody NotificationSettingsUpdateRequest request) {
        return ResponseEntity.ok(
                CommonResponse.success(
                        "알림 설정 변경 성공",
                        notificationService.updateSettings(user.getId(), request)));
    }
}

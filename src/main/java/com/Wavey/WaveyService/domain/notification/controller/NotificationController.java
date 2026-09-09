package com.Wavey.WaveyService.domain.notification.controller;

import com.Wavey.WaveyService.domain.notification.service.NotificationService;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.global.response.ApiResponse;

import jakarta.validation.constraints.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/me/notifications")
public class NotificationController {
    private final NotificationService service;

    @GetMapping
    public ApiResponse<NotificationService.Inbox> list(
            @AuthenticationPrincipal User u,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String language) {
        return ApiResponse.success("알림", service.inbox(u.getId(), page, size, language));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<Void> read(@AuthenticationPrincipal User u, @PathVariable Long id) {
        service.read(u.getId(), id);
        return ApiResponse.success("읽음 처리", null);
    }

    @PatchMapping("/read-all")
    public ApiResponse<Integer> all(@AuthenticationPrincipal User u) {
        return ApiResponse.success("모두 읽음", service.readAll(u.getId()));
    }
}

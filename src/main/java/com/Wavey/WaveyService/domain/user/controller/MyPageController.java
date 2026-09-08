package com.Wavey.WaveyService.domain.user.controller;

import com.Wavey.WaveyService.domain.spot.dto.response.SpotListResponse;
import com.Wavey.WaveyService.domain.user.dto.*;
import com.Wavey.WaveyService.domain.user.entity.*;
import com.Wavey.WaveyService.domain.user.service.MyPageService;
import com.Wavey.WaveyService.global.response.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/me")
public class MyPageController {
    private final MyPageService service;

    @GetMapping
    public ApiResponse<MyPageService.Profile> profile(@AuthenticationPrincipal User u) {
        return ApiResponse.success("마이페이지", service.profile(u.getId()));
    }

    @PatchMapping
    public ApiResponse<MyPageService.Profile> update(
            @AuthenticationPrincipal User u, @Valid @RequestBody ProfileRequest r) {
        return ApiResponse.success("프로필 수정", service.updateProfile(u.getId(), r));
    }

    @GetMapping("/settings")
    public ApiResponse<UserSettings> settings(@AuthenticationPrincipal User u) {
        return ApiResponse.success("설정", service.settings(u.getId()));
    }

    @PatchMapping("/settings")
    public ApiResponse<UserSettings> settings(
            @AuthenticationPrincipal User u, @Valid @RequestBody SettingsRequest r) {
        return ApiResponse.success("설정 저장", service.updateSettings(u.getId(), r));
    }

    @GetMapping("/saved-spots")
    public ApiResponse<Page<SpotListResponse>> saved(
            @AuthenticationPrincipal User u,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String language) {
        return ApiResponse.success("저장한 스팟", service.saved(u.getId(), page, size, language));
    }

    @PutMapping("/saved-spots/{spotId}")
    public ApiResponse<Void> save(@AuthenticationPrincipal User u, @PathVariable Long spotId) {
        service.save(u.getId(), spotId, true);
        return ApiResponse.success("스팟 저장", null);
    }

    @DeleteMapping("/saved-spots/{spotId}")
    public ApiResponse<Void> remove(@AuthenticationPrincipal User u, @PathVariable Long spotId) {
        service.save(u.getId(), spotId, false);
        return ApiResponse.success("저장 해제", null);
    }
}

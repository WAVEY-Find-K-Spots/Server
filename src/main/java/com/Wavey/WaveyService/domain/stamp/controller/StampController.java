package com.Wavey.WaveyService.domain.stamp.controller;

import com.Wavey.WaveyService.domain.stamp.dto.StampClaimRequest;
import com.Wavey.WaveyService.domain.stamp.service.StampService;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.global.response.ApiResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StampController {
    private final StampService service;

    @PostMapping("/spots/{spotId}/stamp")
    public ApiResponse<StampService.Claim> claim(
            @PathVariable Long spotId,
            @AuthenticationPrincipal User u,
            @Valid @RequestBody StampClaimRequest r,
            @RequestParam(required = false) String language) {
        return ApiResponse.success("스탬프 획득", service.claim(spotId, u.getId(), r, language));
    }

    @GetMapping("/me/stamps")
    public ApiResponse<StampService.Book> book(
            @AuthenticationPrincipal User u,
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) String language) {
        return ApiResponse.success("스탬프북", service.book(u.getId(), regionId, language));
    }

    @GetMapping("/me/stamps/{id}")
    public ApiResponse<StampService.StampItem> detail(
            @PathVariable Long id,
            @AuthenticationPrincipal User u,
            @RequestParam(required = false) String language) {
        return ApiResponse.success("스탬프 상세", service.detail(id, u.getId(), language));
    }

    @GetMapping("/me/badges")
    public ApiResponse<List<StampService.BadgeItem>> badges(
            @AuthenticationPrincipal User u, @RequestParam(required = false) String language) {
        return ApiResponse.success("배지 컬렉션", service.badgeList(u.getId(), language));
    }
}

package com.Wavey.WaveyService.domain.review.controller;

import com.Wavey.WaveyService.domain.review.dto.*;
import com.Wavey.WaveyService.domain.review.service.ReviewService;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.global.response.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1")
public class ReviewController {
    private final ReviewService service;

    @PostMapping("/spots/{spotId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReviewResponse> create(
            @PathVariable Long spotId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ReviewRequest request) {
        return ApiResponse.success(201, "리뷰 작성 성공", service.create(spotId, user.getId(), request));
    }

    @GetMapping("/spots/{spotId}/reviews")
    public ApiResponse<Page<ReviewResponse>> list(
            @PathVariable Long spotId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponse.success("리뷰 목록", service.list(spotId, page, size));
    }

    @GetMapping("/me/reviews")
    public ApiResponse<Page<ReviewResponse>> mine(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponse.success("작성한 리뷰", service.mine(user.getId(), page, size));
    }
}

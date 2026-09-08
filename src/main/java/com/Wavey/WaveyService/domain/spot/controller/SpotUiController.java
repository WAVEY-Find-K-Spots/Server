package com.Wavey.WaveyService.domain.spot.controller;

import com.Wavey.WaveyService.domain.content.service.SpotContentService;
import com.Wavey.WaveyService.domain.spot.dto.request.SpotSearchRequest;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotListResponse;
import com.Wavey.WaveyService.domain.spot.service.SpotDiscoveryService;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.global.response.ApiResponse;

import jakarta.validation.constraints.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/spots/{spotId}")
public class SpotUiController {
    private final SpotDiscoveryService discovery;
    private final SpotContentService contents;

    @GetMapping("/contents")
    public ApiResponse<SpotContentService.Result> contents(
            @PathVariable Long spotId,
            @AuthenticationPrincipal User u,
            @RequestParam(required = false) String language) {
        return ApiResponse.success("연관 콘텐츠", contents.get(spotId, u.getId(), language));
    }

    @GetMapping("/nearby")
    public ApiResponse<List<SpotListResponse>> nearby(
            @PathVariable Long spotId,
            @AuthenticationPrincipal User u,
            @RequestParam(defaultValue = "5000") @Min(1) @Max(100000) double radiusMeters,
            @RequestParam(defaultValue = "10") @Min(1) @Max(99) int size,
            @RequestParam(required = false) String language) {
        var spot = discovery.require(spotId);
        var q = new SpotSearchRequest();
        q.setLatitude(spot.getLatitude().doubleValue());
        q.setLongitude(spot.getLongitude().doubleValue());
        q.setRadiusMeters(radiusMeters);
        q.setSort(SpotSearchRequest.SortBy.DISTANCE);
        q.setSize(size + 1);
        q.setLanguage(language);
        return ApiResponse.success(
                "주변 스팟",
                discovery.search(q, u.getId()).stream()
                        .filter(s -> !s.getSpotId().equals(spotId))
                        .limit(size)
                        .toList());
    }
}

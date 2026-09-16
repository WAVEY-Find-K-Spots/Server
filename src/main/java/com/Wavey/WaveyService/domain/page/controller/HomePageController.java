package com.Wavey.WaveyService.domain.page.controller;

import static com.Wavey.WaveyService.global.response.CommonResponse.success;

import com.Wavey.WaveyService.domain.page.service.HomePageService;
import com.Wavey.WaveyService.domain.spot.dto.request.SpotSearchRequest;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotPageResponse;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.global.response.CommonResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Page", description = "화면 단위 API")
@RestController
@RequestMapping("/api/v1/pages/home")
@RequiredArgsConstructor
@Validated
public class HomePageController {

    private final HomePageService homePageService;

    @Operation(
            summary = "홈 화면 스팟 목록/검색",
            description = "홈 화면에서 사용하는 스팟 목록 API입니다. 카테고리, 키워드, 지역, 평점, 정렬(거리순 포함)을 지원합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = SpotPageResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/spots")
    public ResponseEntity<CommonResponse<SpotPageResponse>> getHomeSpots(
            @ParameterObject @Valid @ModelAttribute
            SpotSearchRequest request,

            @Parameter(hidden = true) @AuthenticationPrincipal
            User user
    ) {
        return ResponseEntity.ok(
                success(
                        "홈 화면 스팟 조회 성공",
                        homePageService.getHomeSpots(
                                request,
                                userId(user)
                        )
                )
        );
    }

    private Long userId(User user) {
        return user == null
                ? null
                : user.getId();
    }
}

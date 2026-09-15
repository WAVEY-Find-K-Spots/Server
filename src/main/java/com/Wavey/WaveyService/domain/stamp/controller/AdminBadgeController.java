package com.Wavey.WaveyService.domain.stamp.controller;

import com.Wavey.WaveyService.domain.stamp.dto.request.BadgeCreateRequest;
import com.Wavey.WaveyService.domain.stamp.dto.request.BadgeUpdateRequest;
import com.Wavey.WaveyService.domain.stamp.dto.response.BadgeAdminResponse;
import com.Wavey.WaveyService.domain.stamp.service.BadgeAdminService;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.global.response.CommonResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Badge", description = "배지 관리자 CRUD (집계형·세트형)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/badges")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBadgeController {

    private final BadgeAdminService badgeAdminService;

    @PostMapping
    @Operation(
            summary = "배지 생성",
            description =
                    "집계형 또는 세트형 배지를 생성합니다. "
                            + "spotIds가 있으면 지정 Spot만 카운트(세트형), 없으면 regionId/category 집계형. "
                            + "이미지는 POST /api/v1/uploads/presigned-url (category=BADGE) 후 fileUrl을 넣습니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청/이미지 URL/requiredStamps>spotIds"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
        @ApiResponse(responseCode = "404", description = "regionId 또는 spotId가 존재하지 않음")
    })
    public ResponseEntity<CommonResponse<BadgeAdminResponse>> create(
            @Parameter(hidden = true) @AuthenticationPrincipal User admin,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            required = true,
                            content =
                                    @Content(
                                            schema = @Schema(implementation = BadgeCreateRequest.class),
                                            examples = {
                                                @ExampleObject(
                                                        name = "집계형",
                                                        value =
                                                                """
                                                                {
                                                                  "name": "서울 탐험가",
                                                                  "nameEn": "Seoul Explorer",
                                                                  "description": "서울 스팟 5곳 방문",
                                                                  "requiredStamps": 5,
                                                                  "regionId": 1,
                                                                  "category": null,
                                                                  "spotIds": null
                                                                }
                                                                """),
                                                @ExampleObject(
                                                        name = "세트형",
                                                        value =
                                                                """
                                                                {
                                                                  "name": "궁궐 마스터",
                                                                  "nameEn": "Palace Master",
                                                                  "description": "서울 4대 궁궐 모두 방문",
                                                                  "requiredStamps": 4,
                                                                  "regionId": null,
                                                                  "category": null,
                                                                  "spotIds": [10, 11, 12, 13]
                                                                }
                                                                """)
                                            }))
                    @Valid
                    @RequestBody
                    BadgeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        CommonResponse.success(
                                HttpStatus.CREATED.value(),
                                "배지 생성 성공",
                                badgeAdminService.create(admin.getId(), request)));
    }

    @GetMapping
    @Operation(summary = "배지 목록 조회", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    public CommonResponse<List<BadgeAdminResponse>> list() {
        return CommonResponse.success("배지 목록 조회 성공", badgeAdminService.list());
    }

    @GetMapping("/{badgeId}")
    @Operation(summary = "배지 단건 조회", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "배지 없음")
    })
    public CommonResponse<BadgeAdminResponse> get(
            @Parameter(description = "배지 ID", example = "1") @PathVariable Long badgeId) {
        return CommonResponse.success("배지 조회 성공", badgeAdminService.get(badgeId));
    }

    @PatchMapping("/{badgeId}")
    @Operation(
            summary = "배지 수정",
            description =
                    "전달한 필드만 수정합니다. regionId/category를 지우려면 clearRegionId/clearCategory=true. "
                            + "imageUrl 빈 문자열이면 이미지 제거.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청/이미지 URL"),
        @ApiResponse(responseCode = "404", description = "배지 또는 지역 없음")
    })
    public CommonResponse<BadgeAdminResponse> update(
            @Parameter(hidden = true) @AuthenticationPrincipal User admin,
            @PathVariable Long badgeId,
            @Valid @RequestBody BadgeUpdateRequest request) {
        return CommonResponse.success(
                "배지 수정 성공", badgeAdminService.update(admin.getId(), badgeId, request));
    }

    @DeleteMapping("/{badgeId}")
    @Operation(
            summary = "배지 삭제",
            description = "배지와 유저 획득 기록(user_badges)을 함께 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "배지 없음")
    })
    public CommonResponse<Void> delete(@PathVariable Long badgeId) {
        badgeAdminService.delete(badgeId);
        return CommonResponse.success("배지 삭제 성공", null);
    }
}

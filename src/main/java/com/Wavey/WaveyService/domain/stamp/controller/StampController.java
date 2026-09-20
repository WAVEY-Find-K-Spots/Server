package com.Wavey.WaveyService.domain.stamp.controller;

import com.Wavey.WaveyService.domain.stamp.dto.StampClaimRequest;
import com.Wavey.WaveyService.domain.stamp.service.StampService;
import com.Wavey.WaveyService.domain.stamp.service.UserBadgeService;
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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Stamp", description = "스탬프 획득·스탬프북 조회 API (이름/이미지는 Spot 기준, claim 시 lazy 생성)")
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1")
public class StampController {
    private final StampService service;
    private final UserBadgeService userBadges;

    @PostMapping("/spots/{spotId}/stamp")
    @Operation(
            summary = "스탬프 획득",
            description =
                    "현재 위치가 스팟 반경(150m) 안이면 스탬프를 획득합니다. "
                            + "해당 Spot의 Stamp 행이 없으면 이때 생성합니다. "
                            + "이미 획득한 경우 거리 검사 없이 newlyAcquired=false로 반환합니다. "
                            + "배지는 자동 지급되지 않으며, 조건 충족 시 badges.claimable에 나타납니다. "
                            + "수령은 POST /me/badges/{badgeId}/claim 을 사용합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "스탬프 획득 성공(또는 이미 획득함)",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "statusCode": 200,
                                                          "message": "스탬프 획득",
                                                          "data": {
                                                            "stamp": {
                                                              "stampId": 1,
                                                              "spotId": 10,
                                                              "regionId": 1,
                                                              "name": "경복궁",
                                                              "imageUrl": "https://example.com/spots/10.png",
                                                              "acquired": true,
                                                              "acquiredAt": "2026-09-14T12:00:00"
                                                            },
                                                            "newlyAcquired": true,
                                                            "badges": {
                                                              "acquiredCount": 0,
                                                              "claimableCount": 1,
                                                              "inProgressCount": 1,
                                                              "acquired": [],
                                                              "claimable": [
                                                                {
                                                                  "badgeId": 1,
                                                                  "name": "서울 탐험가",
                                                                  "description": "서울 스팟 5곳 방문",
                                                                  "imageUrl": "https://example.com/badges/1.png",
                                                                  "requiredStamps": 5,
                                                                  "progress": 5,
                                                                  "acquiredAt": null
                                                                }
                                                              ],
                                                              "inProgress": [
                                                                {
                                                                  "badgeId": 2,
                                                                  "name": "한강 러버",
                                                                  "description": "한강 스팟 3곳 방문",
                                                                  "imageUrl": "https://example.com/badges/2.png",
                                                                  "requiredStamps": 3,
                                                                  "progress": 1,
                                                                  "acquiredAt": null
                                                                }
                                                              ]
                                                            }
                                                          }
                                                        }
                                                        """))),
        @ApiResponse(
                responseCode = "400",
                description = "좌표 오류 또는 획득 가능 거리 밖(STAMP_TOO_FAR)",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "statusCode": 400,
                                                          "data": null,
                                                          "error": {
                                                            "code": "STAMP_TOO_FAR",
                                                            "message": "스탬프 획득 가능 거리 밖입니다."
                                                          }
                                                        }
                                                        """))),
        @ApiResponse(
                responseCode = "401",
                description = "인증이 필요합니다.",
                content = @Content(mediaType = "application/json")),
        @ApiResponse(
                responseCode = "404",
                description = "스팟/유저를 찾을 수 없음",
                content = @Content(mediaType = "application/json"))
    })
    public CommonResponse<StampService.Claim> claim(
            @Parameter(description = "스탬프를 획득할 스팟 ID", example = "1") @PathVariable Long spotId,
            @Parameter(hidden = true) @AuthenticationPrincipal User u,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "현재 위치 좌표",
                            required = true,
                            content =
                                    @Content(
                                            schema = @Schema(implementation = StampClaimRequest.class),
                                            examples =
                                                    @ExampleObject(
                                                            value =
                                                                    """
                                                                    {
                                                                      "latitude": 37.579617,
                                                                      "longitude": 126.977041
                                                                    }
                                                                    """)))
                    @Valid
                    @RequestBody
                    StampClaimRequest r,
            @Parameter(
                            description = "응답 언어",
                            example = "ko",
                            schema = @Schema(allowableValues = {"ko", "en"}))
                    @RequestParam(required = false)
                    String language) {
        return CommonResponse.success("스탬프 획득", service.claim(spotId, u.getId(), r, language));
    }

    @GetMapping("/me/stamps")
    @Operation(
            summary = "스탬프북 조회",
            description =
                    "모든 Spot을 스탬프 후보로 페이지 조회합니다. "
                            + "내가 획득한 스탬프가 최근 획득 순으로 먼저, 이어서 미획득 Spot이 id 순으로 내려옵니다. "
                            + "이름/이미지는 Spot에서 읽고, acquired는 내 획득 여부입니다. "
                            + "regionId로 지역 필터가 가능합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "스탬프북 조회 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "statusCode": 200,
                                                          "message": "스탬프북",
                                                          "data": {
                                                            "collectedCount": 3,
                                                            "stamps": [
                                                              {
                                                                "stampId": null,
                                                                "spotId": 10,
                                                                "regionId": 1,
                                                                "name": "경복궁",
                                                                "imageUrl": "https://example.com/spots/10.png",
                                                                "acquired": false,
                                                                "acquiredAt": null
                                                              }
                                                            ],
                                                            "page": 0,
                                                            "totalElements": 57,
                                                            "totalPages": 3,
                                                            "hasNext": true
                                                          }
                                                        }
                                                        """))),
        @ApiResponse(
                responseCode = "400",
                description = "language 값이 ko|en이 아님",
                content = @Content(mediaType = "application/json")),
        @ApiResponse(
                responseCode = "401",
                description = "인증이 필요합니다.",
                content = @Content(mediaType = "application/json"))
    })
    public CommonResponse<StampService.Book> book(
            @Parameter(hidden = true) @AuthenticationPrincipal User u,
            @Parameter(description = "지역 ID 필터 (없으면 전체)", example = "1")
                    @RequestParam(required = false)
                    Long regionId,
            @Parameter(
                            description = "응답 언어",
                            example = "ko",
                            schema = @Schema(allowableValues = {"ko", "en"}))
                    @RequestParam(required = false)
                    String language,
            @Parameter(description = "페이지 번호 (0부터)", example = "0")
                    @RequestParam(required = false)
                    @Min(0)
                    Integer page,
            @Parameter(description = "페이지 크기 (기본 20, 최대 100)", example = "20")
                    @RequestParam(required = false)
                    @Min(1)
                    @Max(100)
                    Integer size) {
        return CommonResponse.success(
                "스탬프북", service.book(u.getId(), regionId, language, page, size));
    }

    @GetMapping("/me/stamps/{id}")
    @Operation(
            summary = "스탬프 상세 조회",
            description =
                    "stampId로 단건 조회합니다. 미획득 스탬프는 프론트에서 상세 진입을 막으므로, "
                            + "정상 플로우에서는 claim 이후(stamp 행 존재)에 호출됩니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "스탬프 상세 조회 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "statusCode": 200,
                                                          "message": "스탬프 상세",
                                                          "data": {
                                                            "stampId": 1,
                                                            "spotId": 10,
                                                            "regionId": 1,
                                                            "name": "경복궁",
                                                            "imageUrl": "https://example.com/spots/10.png",
                                                            "acquired": true,
                                                            "acquiredAt": "2026-09-14T12:00:00"
                                                          }
                                                        }
                                                        """))),
        @ApiResponse(
                responseCode = "401",
                description = "인증이 필요합니다.",
                content = @Content(mediaType = "application/json")),
        @ApiResponse(
                responseCode = "404",
                description = "스탬프 또는 스팟을 찾을 수 없음",
                content = @Content(mediaType = "application/json"))
    })
    public CommonResponse<StampService.StampItem> detail(
            @Parameter(description = "스탬프 ID", example = "1") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal User u,
            @Parameter(
                            description = "응답 언어",
                            example = "ko",
                            schema = @Schema(allowableValues = {"ko", "en"}))
                    @RequestParam(required = false)
                    String language) {
        return CommonResponse.success("스탬프 상세", service.detail(id, u.getId(), language));
    }

    @GetMapping("/me/badges")
    @Operation(
            tags = {"Badge"},
            summary = "내 배지함 조회",
            description =
                    "acquired(획득) / claimable(조건 충족·미수령) / inProgress(도전 중) 로 분리합니다. "
                            + "claimable에서 획득 버튼을 누르면 POST /me/badges/{badgeId}/claim 을 호출합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "배지함 조회 성공",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "statusCode": 200,
                                                          "message": "배지함",
                                                          "data": {
                                                            "acquiredCount": 1,
                                                            "claimableCount": 1,
                                                            "inProgressCount": 1,
                                                            "acquired": [
                                                              {
                                                                "badgeId": 1,
                                                                "name": "서울 탐험가",
                                                                "description": "서울 스팟 5곳 방문",
                                                                "imageUrl": "https://example.com/badges/1.png",
                                                                "requiredStamps": 5,
                                                                "progress": 5,
                                                                "acquiredAt": "2026-09-14T12:00:00"
                                                              }
                                                            ],
                                                            "claimable": [
                                                              {
                                                                "badgeId": 3,
                                                                "name": "카페 마스터",
                                                                "description": "카페 3곳 방문",
                                                                "imageUrl": "https://example.com/badges/3.png",
                                                                "requiredStamps": 3,
                                                                "progress": 3,
                                                                "acquiredAt": null
                                                              }
                                                            ],
                                                            "inProgress": [
                                                              {
                                                                "badgeId": 2,
                                                                "name": "한강 러버",
                                                                "description": "한강 스팟 3곳 방문",
                                                                "imageUrl": "https://example.com/badges/2.png",
                                                                "requiredStamps": 3,
                                                                "progress": 1,
                                                                "acquiredAt": null
                                                              }
                                                            ]
                                                          }
                                                        }
                                                        """))),
        @ApiResponse(
                responseCode = "401",
                description = "인증이 필요합니다.",
                content = @Content(mediaType = "application/json"))
    })
    public CommonResponse<UserBadgeService.BadgeCollection> badges(
            @Parameter(hidden = true) @AuthenticationPrincipal User u,
            @Parameter(
                            description = "응답 언어",
                            example = "ko",
                            schema = @Schema(allowableValues = {"ko", "en"}))
                    @RequestParam(required = false)
                    String language) {
        return CommonResponse.success("배지함", userBadges.collection(u.getId(), language));
    }

    @PostMapping("/me/badges/{badgeId}/claim")
    @Operation(
            tags = {"Badge"},
            summary = "배지 수령",
            description =
                    "조건이 충족된 배지를 사용자가 직접 수령합니다. "
                            + "이미 보유한 경우 newlyAcquired=false로 멱등 반환합니다. "
                            + "진행도 미달이면 BADGE_NOT_CLAIMABLE.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "배지 수령 성공(또는 이미 보유)",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "statusCode": 200,
                                                          "message": "배지 수령",
                                                          "data": {
                                                            "badge": {
                                                              "badgeId": 3,
                                                              "name": "카페 마스터",
                                                              "description": "카페 3곳 방문",
                                                              "imageUrl": "https://example.com/badges/3.png",
                                                              "requiredStamps": 3,
                                                              "progress": 3,
                                                              "acquiredAt": "2026-09-15T00:50:00"
                                                            },
                                                            "newlyAcquired": true
                                                          }
                                                        }
                                                        """))),
        @ApiResponse(
                responseCode = "400",
                description = "수령 조건 미달(BADGE_NOT_CLAIMABLE)",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples =
                                        @ExampleObject(
                                                value =
                                                        """
                                                        {
                                                          "statusCode": 400,
                                                          "data": null,
                                                          "error": {
                                                            "code": "BADGE_400_NOT_CLAIMABLE",
                                                            "message": "아직 배지 수령 조건을 충족하지 않았습니다."
                                                          }
                                                        }
                                                        """))),
        @ApiResponse(
                responseCode = "401",
                description = "인증이 필요합니다.",
                content = @Content(mediaType = "application/json")),
        @ApiResponse(
                responseCode = "404",
                description = "배지/유저를 찾을 수 없음",
                content = @Content(mediaType = "application/json"))
    })
    public CommonResponse<UserBadgeService.BadgeClaim> claimBadge(
            @Parameter(description = "수령할 배지 ID", example = "3") @PathVariable Long badgeId,
            @Parameter(hidden = true) @AuthenticationPrincipal User u,
            @Parameter(
                            description = "응답 언어",
                            example = "ko",
                            schema = @Schema(allowableValues = {"ko", "en"}))
                    @RequestParam(required = false)
                    String language) {
        return CommonResponse.success("배지 수령", userBadges.claim(u.getId(), badgeId, language));
    }
}

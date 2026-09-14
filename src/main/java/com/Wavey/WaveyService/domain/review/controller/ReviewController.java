package com.Wavey.WaveyService.domain.review.controller;

import com.Wavey.WaveyService.domain.review.dto.request.ReviewCreateRequest;
import com.Wavey.WaveyService.domain.review.dto.request.ReviewUpdateRequest;
import com.Wavey.WaveyService.domain.review.dto.response.MyReviewListResponse;
import com.Wavey.WaveyService.domain.review.dto.response.ReviewListResponse;
import com.Wavey.WaveyService.domain.review.dto.response.ReviewResponse;
import com.Wavey.WaveyService.domain.review.service.ReviewService;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import com.Wavey.WaveyService.global.response.CommonResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1")
@Tag(name = "Review", description = "장소 리뷰 작성, 조회, 수정, 삭제 API")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/spots/{spotId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "리뷰 작성", description = "특정 장소에 리뷰와 평점을 작성합니다. 한 사용자는 하나의 장소에 하나의 리뷰만 작성할 수 있습니다.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "리뷰 작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 평점/리뷰 형식 오류"),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 장소 또는 사용자"),
            @ApiResponse(responseCode = "409", description = "이미 해당 장소에 리뷰를 작성함")
    })
    public CommonResponse<ReviewResponse> create(
            @Parameter(description = "리뷰를 작성할 장소 ID", example = "1") @PathVariable @Positive Long spotId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        return CommonResponse.success(HttpStatus.CREATED.value(), "리뷰 작성 성공", reviewService.create(spotId, authenticatedUserId(user), request));
    }

    @GetMapping("/spots/{spotId}/reviews")
    @Operation(summary = "장소 리뷰 목록 조회", description = "특정 장소에 작성된 리뷰 목록과 평균 평점, 전체 리뷰 수를 최신순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장소 리뷰 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 페이지 요청"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 장소")
    })
    public CommonResponse<ReviewListResponse> getReviews(
            @Parameter(description = "장소 ID", example = "1") @PathVariable @Positive Long spotId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "페이지당 리뷰 개수", example = "20") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return CommonResponse.success("장소 리뷰 목록 조회 성공", reviewService.getReviews(spotId, page, size));
    }

    @GetMapping("/reviews/{reviewId}")
    @Operation(summary = "리뷰 단건 조회", description = "리뷰 ID를 기준으로 특정 리뷰의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 단건 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 리뷰")
    })
    public CommonResponse<ReviewResponse> getReview(
            @Parameter(description = "리뷰 ID", example = "1") @PathVariable @Positive Long reviewId
    ) {
        return CommonResponse.success("리뷰 조회 성공", reviewService.getReview(reviewId));
    }

    @GetMapping("/me/reviews")
    @Operation(summary = "내 리뷰 목록 조회", description = "현재 로그인한 사용자가 작성한 리뷰 목록을 최신순으로 조회합니다.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 리뷰 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 페이지 요청"),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자")
    })
    public CommonResponse<MyReviewListResponse> getMyReviews(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "페이지당 리뷰 개수", example = "20") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return CommonResponse.success("내 리뷰 목록 조회 성공", reviewService.getMyReviews(authenticatedUserId(user), page, size));
    }

    @PatchMapping("/reviews/{reviewId}")
    @Operation(summary = "리뷰 수정", description = "현재 로그인한 사용자가 작성한 리뷰의 평점 또는 내용을 수정합니다.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 평점 또는 리뷰 내용"),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없거나 수정 권한이 없음")
    })
    public CommonResponse<ReviewResponse> update(
            @Parameter(description = "수정할 리뷰 ID", example = "1") @PathVariable @Positive Long reviewId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        return CommonResponse.success("리뷰 수정 성공", reviewService.update(reviewId, authenticatedUserId(user), request));
    }

    @DeleteMapping("/reviews/{reviewId}")
    @Operation(summary = "리뷰 삭제", description = "현재 로그인한 사용자가 작성한 리뷰를 삭제합니다. 삭제 후 장소의 평균 평점과 리뷰 수가 다시 계산됩니다.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없거나 삭제 권한이 없음")
    })
    public CommonResponse<Void> delete(
            @Parameter(description = "삭제할 리뷰 ID", example = "1") @PathVariable @Positive Long reviewId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user
    ) {
        reviewService.delete(reviewId, authenticatedUserId(user));
        return CommonResponse.success("리뷰 삭제 성공", null);
    }

    private Long authenticatedUserId(User user) {
        if (user == null || user.getId() == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        return user.getId();
    }
}

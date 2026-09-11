package com.Wavey.WaveyService.domain.user.controller;

import com.Wavey.WaveyService.domain.user.dto.LoginCodeExchangeRequest;
import com.Wavey.WaveyService.domain.user.dto.RefreshTokenRequest;
import com.Wavey.WaveyService.domain.user.dto.TokenResponse;
import com.Wavey.WaveyService.domain.user.dto.UserResponse;
import com.Wavey.WaveyService.domain.user.entity.Role;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.domain.user.service.CustomOAuth2UserService;
import com.Wavey.WaveyService.global.response.ApiResponse;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 및 회원 관리 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class Auth2Controller {

    private final CustomOAuth2UserService userService;

    @Operation(summary = "소셜 로그인 진입 URL 조회")
    @GetMapping("/login-urls")
    public ResponseEntity<ApiResponse<Map<String, String>>> getLoginUrls() {
        return ResponseEntity.ok(ApiResponse.success("소셜 로그인 URL 조회 성공", userService.getLoginUrls()));
    }

    @Operation(summary = "일회성 로그인 코드 교환")
    @PostMapping("/exchange")
    public ResponseEntity<ApiResponse<TokenResponse>> exchange(
            @Valid @RequestBody LoginCodeExchangeRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "로그인 토큰 발급 성공",
                userService.exchangeLoginCode(request.code())
        ));
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "토큰 재발급 및 로테이션 성공",
                userService.refreshToken(request.refreshToken())
        ));
    }

    @Operation(summary = "현재 로그인 유저 정보 조회")
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<UserResponse>> getLoginUserInfo(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("로그인 유저 정보 조회 성공", UserResponse.from(user)));
    }

    @Operation(summary = "특정 회원 조회")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("회원 정보 조회 성공", userService.findById(id)));
    }

    @Operation(summary = "유저 권한 수정")
    @PatchMapping("/role/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateRole(@PathVariable Long id, @RequestParam Role role) {
        userService.updateUserRole(id, role);
        return ResponseEntity.ok(ApiResponse.success("유저 권한 수정 성공", null));
    }

    @Operation(summary = "전체 회원 목록 조회")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success("전체 회원 목록 조회 성공", userService.findAllUsers()));
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal User user,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        userService.withdraw(user, resolveBearerToken(authorization));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal User user,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        userService.logout(user, resolveBearerToken(authorization));
        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공. 모든 토큰이 무효화되었습니다.", null));
    }

    private String resolveBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return authorization.substring(7);
    }
}

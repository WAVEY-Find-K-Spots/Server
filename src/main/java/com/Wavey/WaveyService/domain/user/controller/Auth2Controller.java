package com.Wavey.WaveyService.domain.user.controller;

import com.Wavey.WaveyService.domain.user.dto.UserResponse;
import com.Wavey.WaveyService.domain.user.entity.Role;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.domain.user.service.CustomOAuth2UserService;
import com.Wavey.WaveyService.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PreAuthorize("permitAll()")
    public ApiResponse<Map<String, String>> getLoginUrls() {
        return ApiResponse.success("소셜 로그인 URL 조회 성공", userService.getLoginUrls());
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/refresh")
    @PreAuthorize("permitAll()")
    public ApiResponse<Map<String, String>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        return ApiResponse.success("토큰 재발급 및 로테이션 성공", userService.refreshToken(refreshToken));
    }

    @Operation(summary = "현재 로그인 유저 정보 조회")
    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserResponse> getLoginUserInfo(@AuthenticationPrincipal User user) {
        return ApiResponse.success("로그인 유저 정보 조회 성공", UserResponse.from(user));
    }

    @Operation(summary = "특정 회원 조회")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id.equals(authentication.principal.id)")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        return ApiResponse.success("회원 정보 조회 성공", userService.findById(id));
    }

    @Operation(summary = "유저 권한 수정")
    @PatchMapping("/role/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateRole(@PathVariable Long id, @RequestParam Role role) {
        userService.updateUserRole(id, role);
        return ApiResponse.success("유저 권한 수정 성공", null);
    }

    @Operation(summary = "전체 회원 목록 조회")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<User>> getAllUsers() {
        return ApiResponse.success("전체 회원 목록 조회 성공", userService.findAllUsers());
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/withdraw/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> withdraw(@PathVariable Long id) {
        userService.withdraw(id);
        return ApiResponse.success("회원 탈퇴 성공", null);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id.equals(authentication.principal.id)")
    public ApiResponse<Void> logout(@PathVariable Long id) {
        userService.logout(id);
        return ApiResponse.success("로그아웃 성공. 모든 토큰이 무효화되었습니다.", null);
    }
}

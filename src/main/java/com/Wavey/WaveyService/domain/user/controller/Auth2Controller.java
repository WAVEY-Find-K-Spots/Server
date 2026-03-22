package com.Wavey.WaveyService.domain.user.controller;

import com.Wavey.WaveyService.domain.user.entity.Role;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.domain.user.service.CustomOAuth2UserService;
import com.Wavey.WaveyService.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Auth", description = "인증 및 회원 관리 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class Auth2Controller {

    private final CustomOAuth2UserService userService;

    /**
     * 그룹 1: 모두 허용 (PermitAll)
     */
    @Operation(summary = "소셜 로그인 진입 주소 조회")
    @GetMapping("/login-urls")
    @PreAuthorize("permitAll()")
    public CommonResponse<Map<String, String>> getLoginUrls() {
        return CommonResponse.success("소셜 로그인 URL 조회 성공", userService.getLoginUrls());
    }

    @Operation(summary = "토큰 재발급(리프레시 토큰 입력)")
    @PostMapping("/refresh")
    @PreAuthorize("permitAll()")
    public CommonResponse<Map<String, String>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        // 서비스로 로직 이관
        return CommonResponse.success("토큰 재발급 및 로테이션 성공", userService.refreshToken(refreshToken));
    }

    /**
     * 그룹 2: 로그인 유저 (Authenticated)
     */
    @Operation(summary = "현재 로그인 유저 정보 조회")
    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<User> getLoginUserInfo(@AuthenticationPrincipal User user) {
        return CommonResponse.success("로그인 유저 정보 조회 성공", user);
    }

    @Operation(summary = "특정 회원 조회 (ID 기준)")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public CommonResponse<User> getUserById(@PathVariable Long id) {
        return CommonResponse.success("회원 정보 조회 성공", userService.findById(id));
    }

    /**
     * 그룹 3: 어드민 권한 (Admin Only)
     */
    @Operation(summary = "유저 권한 수정 (Admin 전용)")
    @PatchMapping("/role/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CommonResponse<Void> updateRole(@PathVariable Long id, @RequestParam Role role) {
        userService.updateUserRole(id, role);
        return CommonResponse.success("유저 권한 수정 성공", null);
    }

    @Operation(summary = "전체 회원 목록 조회 (Admin 전용)")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public CommonResponse<List<User>> getAllUsers() {
        return CommonResponse.success("전체 회원 목록 조회 성공", userService.findAllUsers());
    }

    @Operation(summary = "회원 탈퇴 (Admin 전용)")
    @DeleteMapping("/withdraw/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CommonResponse<Void> withdraw(@PathVariable Long id) {
        userService.withdraw(id);
        return CommonResponse.success("회원 탈퇴 성공", null);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public CommonResponse<Void> logout(@PathVariable Long id) {
        userService.logout(id);
        return CommonResponse.success("로그아웃 성공. 모든 토큰이 무효화되었습니다.", null);
    }
}
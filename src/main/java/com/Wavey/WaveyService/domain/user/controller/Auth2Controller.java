package com.Wavey.WaveyService.domain.user.controller;

import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.domain.user.service.CustomOAuth2UserService;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import com.Wavey.WaveyService.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Auth", description = "인증 및 회원 관리 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class Auth2Controller {

    private final CustomOAuth2UserService userService;

    @Operation(summary = "소셜 로그인 진입 주소 조회")
    @GetMapping("/login-urls")
    public CommonResponse<Map<String, String>> getLoginUrls() {
        return CommonResponse.success("소셜 로그인 URL 조회 성공", userService.getLoginUrls());
    }

    @Operation(summary = "현재 로그인 유저 정보 조회")
    @GetMapping("/user")
    public CommonResponse<Map<String, Object>> getLoginUserInfo(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            throw new CustomException(ErrorCode.USER_ACCESS_DENIED);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("name", principal.getAttribute("name"));
        response.put("email", principal.getAttribute("email"));
        response.put("providerId", principal.getAttribute("sub"));

        return CommonResponse.success("로그인 유저 정보 조회 성공", response);
    }

    @Operation(summary = "전체 회원 목록 조회")
    @GetMapping("/all")
    public CommonResponse<List<User>> getAllUsers() {
        return CommonResponse.success("전체 회원 목록 조회 성공", userService.findAllUsers());
    }

    @Operation(summary = "특정 회원 조회 (ID 기준)")
    @GetMapping("/{id}")
    public CommonResponse<User> getUserById(@PathVariable Long id) {
        return CommonResponse.success("회원 정보 조회 성공", userService.findById(id));
    }

    @Operation(summary = "회원 탈퇴 (ID 기준)")
    @DeleteMapping("/withdraw/{id}")
    public CommonResponse<Void> withdraw(@PathVariable Long id) {
        userService.withdraw(id);
        return CommonResponse.success("회원 탈퇴 성공 (ID: " + id + ")", null);
    }
}
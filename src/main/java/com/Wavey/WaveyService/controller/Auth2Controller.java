package com.Wavey.WaveyService.controller;

import com.Wavey.WaveyService.entity.User;
import com.Wavey.WaveyService.service.CustomOAuth2UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
@CrossOrigin(origins = "*")
public class Auth2Controller {

    private final CustomOAuth2UserService userService;

    @Operation(summary = "소셜 로그인 진입 주소 조회", description = "서비스 로직을 통해 생성된 구글/애플 로그인 전체 URL을 반환합니다.")
    @GetMapping("/login-urls")
    public ResponseEntity<Map<String, String>> getLoginUrls() {
        return ResponseEntity.ok(userService.getLoginUrls());
    }

    @Operation(summary = "현재 로그인 유저 정보 조회")
    @GetMapping("/user")
    public ResponseEntity<?> getLoginUserInfo(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).body("로그인 필요");

        Map<String, Object> response = new HashMap<>();
        response.put("name", principal.getAttribute("name"));
        response.put("email", principal.getAttribute("email"));
        response.put("providerId", principal.getAttribute("sub"));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "전체 회원 목록 조회")
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @Operation(summary = "특정 회원 조회 (ID 기준)")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @Operation(summary = "회원 탈퇴 (ID 기준)")
    @DeleteMapping("/withdraw/{id}")
    public ResponseEntity<String> withdraw(@PathVariable Long id) {
        userService.withdraw(id);
        return ResponseEntity.ok("탈퇴 완료 (ID: " + id + ")");
    }
}
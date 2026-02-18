package com.Wavey.WaveyService.controller;

import com.Wavey.WaveyService.entity.User;
import com.Wavey.WaveyService.repository.UserRepository;
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
@RequiredArgsConstructor // UserRepository 주입을 위해 추가
public class Auth2Controller {

    private final UserRepository userRepository;

    @Operation(summary = "현재 로그인 유저 정보 조회", description = "세션에 저장된 현재 로그인 사용자의 상세 정보를 반환합니다.")
    @GetMapping("/user")
    public Map<String, Object> getLoginUserInfo(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> response = new HashMap<>();

        if (principal == null) {
            response.put("error", "로그인 상태가 아닙니다.");
            return response;
        }

        response.put("name", principal.getAttribute("name"));
        response.put("email", principal.getAttribute("email"));
        response.put("sub", principal.getAttribute("sub"));
        response.put("provider", "google");

        return response;
    }

    @Operation(summary = "전체 회원 목록 조회", description = "DB(H2/PostgreSQL)에 저장된 모든 회원 데이터를 조회합니다.")
    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Operation(summary = "특정 회원 조회 (sub 기준)", description = "구글 고유 번호(sub)를 이용해 특정 회원을 검색합니다.")
    @GetMapping("/{sub}")
    public User getUserBySub(@PathVariable String sub) {
        return userRepository.findBySub(sub)
                .orElseThrow(() -> new RuntimeException("해당하는 회원을 찾을 수 없습니다."));
    }

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 정보를 DB에서 삭제합니다.")
    @DeleteMapping("/withdraw")
    public String withdraw(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return "로그인이 필요합니다.";

        String sub = principal.getAttribute("sub");
        userRepository.findBySub(sub).ifPresent(userRepository::delete);

        return "회원 탈퇴가 완료되었습니다. (sub: " + sub + ")";
    }
}
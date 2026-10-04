package com.Wavey.WaveyService.domain.user.controller;

import com.Wavey.WaveyService.domain.user.dto.LocalLoginRequest;
import com.Wavey.WaveyService.domain.user.dto.LocalPasswordChangeRequest;
import com.Wavey.WaveyService.domain.user.dto.LocalPasswordResetConfirmRequest;
import com.Wavey.WaveyService.domain.user.dto.LocalPasswordResetRequest;
import com.Wavey.WaveyService.domain.user.dto.LocalSignupRequest;
import com.Wavey.WaveyService.domain.user.dto.LoginCodeExchangeRequest;
import com.Wavey.WaveyService.domain.user.dto.PhotoConfirmRequest;
import com.Wavey.WaveyService.domain.user.dto.UserProfileUpdateRequest;
import com.Wavey.WaveyService.domain.user.dto.RefreshTokenRequest;
import com.Wavey.WaveyService.domain.user.dto.TokenResponse;
import com.Wavey.WaveyService.domain.user.dto.UserResponse;
import com.Wavey.WaveyService.domain.user.enums.Role;
import com.Wavey.WaveyService.domain.user.entity.User;
import com.Wavey.WaveyService.domain.user.service.CustomOAuth2UserService;
import com.Wavey.WaveyService.global.response.CommonResponse;
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
import org.springframework.http.HttpStatus;
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

@Tag(name = "User Auth", description = "인증 및 회원 관리 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class Auth2Controller {

    private final CustomOAuth2UserService userService;

    @Operation(summary = "소셜 로그인 진입 URL 조회")
    @GetMapping("/login-urls")
    public ResponseEntity<CommonResponse<Map<String, String>>> getLoginUrls() {
        return ResponseEntity.ok(CommonResponse.success("소셜 로그인 URL 조회 성공", userService.getLoginUrls()));
    }

    @Operation(summary = "로컬 회원가입", description = "이메일과 비밀번호로 USER 권한 계정을 만들고 기존 JWT 토큰 쌍을 발급합니다.")
    @PostMapping("/local/signup")
    public ResponseEntity<CommonResponse<TokenResponse>> signupLocal(
            @Valid @RequestBody LocalSignupRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(
                HttpStatus.CREATED.value(),
                "로컬 회원가입 성공",
                userService.signupLocal(request)
        ));
    }

    @Operation(summary = "로컬 로그인", description = "로컬 계정 인증 후 소셜 로그인과 동일한 Access/Refresh Token을 발급합니다.")
    @PostMapping("/local/login")
    public ResponseEntity<CommonResponse<TokenResponse>> loginLocal(
            @Valid @RequestBody LocalLoginRequest request
    ) {
        return ResponseEntity.ok(CommonResponse.success(
                "로컬 로그인 성공",
                userService.loginLocal(request)
        ));
    }

    @Operation(
            summary = "비밀번호 재설정 메일 요청",
            description = "가입 여부와 관계없이 동일하게 응답하며, 로컬 계정이면 일회용 재설정 링크를 SMTP로 발송합니다."
    )
    @PostMapping("/local/password/reset/request")
    public ResponseEntity<CommonResponse<Void>> requestLocalPasswordReset(
            @Valid @RequestBody LocalPasswordResetRequest request
    ) {
        userService.requestLocalPasswordReset(request);
        return ResponseEntity.ok(CommonResponse.success(
                "가입된 로컬 계정인 경우 비밀번호 재설정 메일을 발송했습니다.",
                null
        ));
    }

    @Operation(
            summary = "비밀번호 재설정 확정",
            description = "메일로 전달된 일회용 토큰을 검증하고 새 비밀번호로 변경합니다."
    )
    @PostMapping("/local/password/reset/confirm")
    public ResponseEntity<CommonResponse<Void>> confirmLocalPasswordReset(
            @Valid @RequestBody LocalPasswordResetConfirmRequest request
    ) {
        userService.confirmLocalPasswordReset(request);
        return ResponseEntity.ok(CommonResponse.success(
                "비밀번호 재설정 성공. 새 비밀번호로 로그인해 주세요.",
                null
        ));
    }

    @Operation(
            summary = "로컬 계정 비밀번호 변경",
            description = "Access Token의 이메일과 로그인 계정을 검증하고 현재 비밀번호 확인 후 변경합니다. 변경 즉시 기존 토큰은 모두 무효화됩니다."
    )
    @PatchMapping("/local/password")
    public ResponseEntity<CommonResponse<Void>> changeLocalPassword(
            @AuthenticationPrincipal User user,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @Valid @RequestBody LocalPasswordChangeRequest request
    ) {
        userService.changeLocalPassword(user.getId(), resolveBearerToken(authorization), request);
        return ResponseEntity.ok(CommonResponse.success(
                "비밀번호 변경 성공. 새 비밀번호로 다시 로그인해 주세요.",
                null
        ));
    }

    @Operation(summary = "일회성 로그인 코드 교환")
    @PostMapping("/exchange")
    public ResponseEntity<CommonResponse<TokenResponse>> exchange(
            @Valid @RequestBody LoginCodeExchangeRequest request
    ) {
        return ResponseEntity.ok(CommonResponse.success(
                "로그인 토큰 발급 성공",
                userService.exchangeLoginCode(request.code())
        ));
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<TokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(CommonResponse.success(
                "토큰 재발급 및 로테이션 성공",
                userService.refreshToken(request.refreshToken())
        ));
    }

    @Operation(summary = "현재 로그인 유저 정보 조회")
    @GetMapping("/user")
    public ResponseEntity<CommonResponse<UserResponse>> getLoginUserInfo(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(CommonResponse.success("로그인 유저 정보 조회 성공", userService.toResponse(user)));
    }

    @Operation(summary = "프로필 사진 반영", description = "업로드가 끝난 사진 URL을 프로필에 반영합니다. 업로드 URL은 POST /api/v1/uploads/presigned-url(category=PROFILE)로 발급받습니다.")
    @PatchMapping("/user/photo")
    public ResponseEntity<CommonResponse<UserResponse>> confirmPhoto(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PhotoConfirmRequest request
    ) {
        return ResponseEntity.ok(CommonResponse.success(
                "프로필 사진 반영 성공",
                userService.confirmPhoto(user.getId(), request.photoUrl())
        ));
    }

    @Operation(
            summary = "프로필 수정",
            description = "닉네임/국적/언어/위치 서비스 이용 여부/마케팅 정보 수신 여부를 부분 수정합니다. "
                    + "전달한 필드만 반영되며, email/name은 OAuth 값으로만 동기화되어 여기서 수정할 수 없습니다."
    )
    @PatchMapping("/user")
    public ResponseEntity<CommonResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(CommonResponse.success(
                "프로필 수정 성공",
                userService.updateProfile(user.getId(), request)
        ));
    }

    @Operation(summary = "특정 회원 조회")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id.equals(authentication.principal.id)")
    public ResponseEntity<CommonResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(CommonResponse.success("회원 정보 조회 성공", userService.findById(id)));
    }

    @Operation(summary = "유저 권한 수정")
    @PatchMapping("/role/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Void>> updateRole(@PathVariable Long id, @RequestParam Role role) {
        userService.updateUserRole(id, role);
        return ResponseEntity.ok(CommonResponse.success("유저 권한 수정 성공", null));
    }

    @Operation(summary = "전체 회원 목록 조회")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(CommonResponse.success("전체 회원 목록 조회 성공", userService.findAllUsers()));
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
    public ResponseEntity<CommonResponse<Void>> logout(
            @AuthenticationPrincipal User user,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        userService.logout(user, resolveBearerToken(authorization));
        return ResponseEntity.ok(CommonResponse.success("로그아웃 성공. 모든 토큰이 무효화되었습니다.", null));
    }

    private String resolveBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return authorization.substring(7);
    }
}

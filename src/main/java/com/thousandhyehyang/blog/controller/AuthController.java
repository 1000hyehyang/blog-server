package com.thousandhyehyang.blog.controller;

import com.thousandhyehyang.blog.common.ApiResponse;
import com.thousandhyehyang.blog.dto.TokenRefreshRequest;
import com.thousandhyehyang.blog.dto.TokenRefreshResponse;
import com.thousandhyehyang.blog.entity.Account;
import com.thousandhyehyang.blog.security.RedisTokenService;
import com.thousandhyehyang.blog.security.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "인증 관련 API")
public class AuthController {

    private final TokenProvider tokenProvider;
    private final RedisTokenService redisTokenService;

    public AuthController(TokenProvider tokenProvider, RedisTokenService redisTokenService) {
        this.tokenProvider = tokenProvider;
        this.redisTokenService = redisTokenService;
    }

    /**
     * 액세스 토큰 재발급
     * @param refreshToken 쿠키에서 추출한 리프레시 토큰
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "액세스 토큰 재발급", description = "HttpOnly 쿠키의 Refresh Token으로 Access Token을 재발급합니다.")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(@CookieValue(name = "refreshToken") String refreshToken) {

        // 리프레시 토큰 검증
        if (!tokenProvider.validateToken(refreshToken) || !tokenProvider.isRefreshToken(refreshToken)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid refresh token"));
        }

        // 토큰에서 사용자 ID 가져오기
        Long userId = tokenProvider.getUserIdFromToken(refreshToken);

        // Redis에서 리프레시 토큰 검증
        if (!redisTokenService.validateRefreshToken(userId, refreshToken)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid or expired refresh token"));
        }

        // 새 액세스 토큰 생성
        String newAccessToken = tokenProvider.createAccessToken(userId);

        return ResponseEntity.ok(ApiResponse.success(new TokenRefreshResponse(newAccessToken)));
    }

    /**
     * 로그아웃 처리 (리프레시 토큰 제거 및 쿠키 만료)
     * @param account 인증된 사용자 계정
     * @param response HTTP 응답 객체
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 로그인한 사용자의 리프레시 토큰을 무효화하고 쿠키를 만료시킵니다.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal Account account,
            HttpServletResponse response) {
        if (account != null) {
            // Redis에서 리프레시 토큰 삭제
            redisTokenService.deleteRefreshToken(account.getId());

            // 리프레시 토큰 쿠키 삭제
            Cookie cookie = new Cookie("refreshToken", "");
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(0); // 즉시 만료
            // 프로덕션 환경에서는 secure 플래그를 true로 설정해야 합니다
            // cookie.setSecure(true);
            response.addCookie(cookie);

            return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
        }

        return ResponseEntity.badRequest().body(ApiResponse.error("Not authenticated"));
    }
}

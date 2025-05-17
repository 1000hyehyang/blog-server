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
     * @param request 리프레시 토큰 포함 요청
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "액세스 토큰 재발급", description = "Refresh Token으로 Access Token을 재발급합니다.")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(@RequestBody TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        // Validate refresh token
        if (!tokenProvider.validateToken(refreshToken) || !tokenProvider.isRefreshToken(refreshToken)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid refresh token"));
        }

        // Get user ID from token
        Long userId = tokenProvider.getUserIdFromToken(refreshToken);

        // Validate refresh token in Redis
        if (!redisTokenService.validateRefreshToken(userId, refreshToken)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid or expired refresh token"));
        }

        // Generate new access token
        String newAccessToken = tokenProvider.createAccessToken(userId);

        return ResponseEntity.ok(ApiResponse.success(new TokenRefreshResponse(newAccessToken)));
    }

    /**
     * 로그아웃 처리 (리프레시 토큰 제거)
     * @param account 인증된 사용자 계정
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 로그인한 사용자의 리프레시 토큰을 무효화합니다.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal Account account) {
        if (account != null) {
            redisTokenService.deleteRefreshToken(account.getId());
            return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
        }

        return ResponseEntity.badRequest().body(ApiResponse.error("Not authenticated"));
    }
}

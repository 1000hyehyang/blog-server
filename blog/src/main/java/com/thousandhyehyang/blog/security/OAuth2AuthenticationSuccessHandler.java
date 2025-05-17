package com.thousandhyehyang.blog.security;

import com.thousandhyehyang.blog.config.JwtProperties;
import com.thousandhyehyang.blog.entity.Account;
import com.thousandhyehyang.blog.repository.AccountRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;
    private final RedisTokenService redisTokenService;
    private final AccountRepository accountRepository;
    private final JwtProperties jwtProperties;

    // 인증 성공 후 리다이렉트할 프론트엔드 URL
    private static final String REDIRECT_URI = "http://localhost:5173/";

    public OAuth2AuthenticationSuccessHandler(
            TokenProvider tokenProvider,
            RedisTokenService redisTokenService,
            AccountRepository accountRepository,
            JwtProperties jwtProperties) {
        this.tokenProvider = tokenProvider;
        this.redisTokenService = redisTokenService;
        this.accountRepository = accountRepository;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        // Authentication 객체에서 직접 Account 엔티티 가져오기
        Account account = (Account) authentication.getPrincipal();

        // 토큰 생성
        String accessToken = tokenProvider.createAccessToken(account.getId());
        String refreshToken = tokenProvider.createRefreshToken(account.getId());

        // Redis에 리프레시 토큰 저장
        redisTokenService.storeRefreshToken(account.getId(), refreshToken);

        // 리프레시 토큰을 HttpOnly 쿠키로 설정
        jakarta.servlet.http.Cookie refreshTokenCookie = new jakarta.servlet.http.Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (jwtProperties.getRefreshTokenExpirationMs() / 1000));
        // 프로덕션 환경에서는 secure 플래그를 true로 설정하는 것이 좋습니다
        // refreshTokenCookie.setSecure(true);
        response.addCookie(refreshTokenCookie);

        // 액세스 토큰만 포함된 리다이렉트 URL 생성
        String targetUrl = UriComponentsBuilder.fromUriString(REDIRECT_URI)
                .queryParam("accessToken", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    // Authentication 객체에서 직접 Account를 가져오므로 사용하지 않는 메서드 제거
}

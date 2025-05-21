package com.thousandhyehyang.blog.security;

import com.thousandhyehyang.blog.config.JwtProperties;
import com.thousandhyehyang.blog.entity.Account;
import com.thousandhyehyang.blog.repository.AccountRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    private final TokenProvider tokenProvider;
    private final RedisTokenService redisTokenService;
    private final JwtProperties jwtProperties;
    private final String redirectUri;
    private final Environment environment;

    public OAuth2AuthenticationSuccessHandler(
            TokenProvider tokenProvider,
            RedisTokenService redisTokenService,
            JwtProperties jwtProperties,
            Environment environment,
            @Value("${spring.app.oauth2.redirect-uri}") String redirectUri
    ) {
        this.tokenProvider = tokenProvider;
        this.redisTokenService = redisTokenService;
        this.jwtProperties = jwtProperties;
        this.environment = environment;
        this.redirectUri = redirectUri;
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
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (jwtProperties.getRefreshTokenExpirationMs() / 1000));

        // 프로덕션 환경에서는 Secure 플래그 활성화
        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        refreshTokenCookie.setSecure(isProd);

        if (isProd) {
            logger.info("프로덕션 환경: 리프레시 토큰 쿠키에 Secure 플래그 활성화");
        } else {
            logger.debug("개발 환경: 리프레시 토큰 쿠키에 Secure 플래그 비활성화");
        }

        response.addCookie(refreshTokenCookie);

        // 액세스 토큰만 포함된 리다이렉트 URL 생성
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

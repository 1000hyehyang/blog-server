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

    // Frontend URL to redirect to after successful authentication
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

        // Get the Account entity directly from the Authentication object
        Account account = (Account) authentication.getPrincipal();

        // Generate tokens
        String accessToken = tokenProvider.createAccessToken(account.getId());
        String refreshToken = tokenProvider.createRefreshToken(account.getId());

        // Store refresh token in Redis
        redisTokenService.storeRefreshToken(account.getId(), refreshToken);

        // Set refresh token as HttpOnly cookie
        jakarta.servlet.http.Cookie refreshTokenCookie = new jakarta.servlet.http.Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (jwtProperties.getRefreshTokenExpirationMs() / 1000));
        // In production, you might want to set secure flag to true
        // refreshTokenCookie.setSecure(true);
        response.addCookie(refreshTokenCookie);

        // Build redirect URL with access token only
        String targetUrl = UriComponentsBuilder.fromUriString(REDIRECT_URI)
                .queryParam("accessToken", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    // Removed unused methods as we now get the Account directly from the Authentication object
}

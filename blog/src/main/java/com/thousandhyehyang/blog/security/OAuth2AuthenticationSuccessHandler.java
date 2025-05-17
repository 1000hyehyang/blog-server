package com.thousandhyehyang.blog.security;

import com.thousandhyehyang.blog.entity.Account;
import com.thousandhyehyang.blog.repository.AccountRepository;
import jakarta.servlet.ServletException;
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

    // Frontend URL to redirect to after successful authentication
    private static final String REDIRECT_URI = "http://localhost:5173/oauth/callback";

    public OAuth2AuthenticationSuccessHandler(
            TokenProvider tokenProvider,
            RedisTokenService redisTokenService,
            AccountRepository accountRepository) {
        this.tokenProvider = tokenProvider;
        this.redisTokenService = redisTokenService;
        this.accountRepository = accountRepository;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // Get provider
        String provider = getProvider(request);

        // Extract email and username based on provider
        String email = getEmail(attributes, provider);
        String username = getUserId(attributes, provider);

        // Find account by provider and email/username
        Optional<Account> accountOptional = findAccount(provider, email, username);

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();

            // Generate tokens
            String accessToken = tokenProvider.createAccessToken(account.getId());
            String refreshToken = tokenProvider.createRefreshToken(account.getId());

            // Store refresh token in Redis
            redisTokenService.storeRefreshToken(account.getId(), refreshToken);

            // Build redirect URL with tokens
            String targetUrl = UriComponentsBuilder.fromUriString(REDIRECT_URI)
                    .queryParam("accessToken", accessToken)
                    .queryParam("refreshToken", refreshToken)
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } else {
            // This should not happen as the account should have been created in CustomOAuth2UserService
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Account not found");
        }
    }

    private String getProvider(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.contains("google")) {
            return "google";
        } else if (uri.contains("github")) {
            return "github";
        }
        return "unknown";
    }

    private String getEmail(Map<String, Object> attributes, String provider) {
        if ("google".equals(provider)) {
            return (String) attributes.get("email");
        } else if ("github".equals(provider)) {
            return (String) attributes.get("email");
        }
        return null;
    }

    private String getUserId(Map<String, Object> attributes, String provider) {
        if ("google".equals(provider)) {
            return (String) attributes.get("sub");
        } else if ("github".equals(provider)) {
            return ((Integer) attributes.get("id")).toString();
        }
        return null;
    }

    private Optional<Account> findAccount(String provider, String email, String username) {
        // Find by email regardless of provider
        if (email != null) {
            Optional<Account> accountByEmail = accountRepository.findByEmail(email);
            if (accountByEmail.isPresent()) {
                return accountByEmail;
            }
        }

        return Optional.empty();
    }
}

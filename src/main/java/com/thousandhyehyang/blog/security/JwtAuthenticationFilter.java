package com.thousandhyehyang.blog.security;

import com.thousandhyehyang.blog.entity.Account;
import com.thousandhyehyang.blog.repository.AccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;
    private final AccountRepository accountRepository;

    public JwtAuthenticationFilter(TokenProvider tokenProvider, AccountRepository accountRepository) {
        this.tokenProvider = tokenProvider;
        this.accountRepository = accountRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            System.out.println("🔐 Extracted JWT: " + jwt);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt) && !tokenProvider.isRefreshToken(jwt)) {
                Long accountId = tokenProvider.getUserIdFromToken(jwt);
                System.out.println("✅ 토큰 유저 ID: " + accountId);

                Optional<Account> accountOptional = accountRepository.findById(accountId);
                if (accountOptional.isPresent()) {
                    Account account = accountOptional.get();
                    logger.info("✅ Jwt 필터 - 인증 대상 계정 ID: {}", account.getId());
                    logger.info("✅ Jwt 필터 - 인증 대상 권한: {}", account.getAuthorities());

                    // Ensure attributes is initialized
                    if (account.getAttributes() == null) {
                        logger.warn("Account attributes was null, initializing empty map");
                        account.setAttributes(new HashMap<>());
                    }

                    // Account 엔티티의 권한으로 인증 토큰 생성
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            account,
                            null,
                            account.getAuthorities()
                    );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("✅ SecurityContext 설정 완료됨!");
                } else {
                    logger.warn("No account found for ID: {}", accountId);
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
            ex.printStackTrace();
        }

        System.out.println("✅ Jwt 필터 끝. 다음 필터로 넘깁니다.");
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}

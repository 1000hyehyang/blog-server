package com.thousandhyehyang.blog.security;

import com.thousandhyehyang.blog.entity.Account;
import com.thousandhyehyang.blog.repository.AccountRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
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

/**
 * JWT 인증 필터
 * - 요청마다 실행되며, JWT 토큰을 검증하고 인증 정보를 SecurityContext에 저장함
 */
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

            // 토큰이 존재하고 유효하며, access 토큰인 경우에만 처리
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt) && !tokenProvider.isRefreshToken(jwt)) {
                Long accountId = tokenProvider.getUserIdFromToken(jwt);
                logger.debug("JWT 인증 시도: accountId={}, URI={}", accountId, request.getRequestURI());

                Optional<Account> accountOptional = accountRepository.findById(accountId);
                if (accountOptional.isPresent()) {
                    Account account = accountOptional.get();

                    if (account.getAttributes() == null) {
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
                    logger.debug("인증 성공: accountId={}, username={}", accountId, account.getUsername());
                } else {
                    logger.warn("유효한 토큰이지만 계정을 찾을 수 없음: accountId={}", accountId);
                }
            }
        } catch (ExpiredJwtException e) {
            logger.warn("만료된 JWT 토큰: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.warn("잘못된 형식의 JWT 토큰: {}", e.getMessage());
        } catch (SignatureException e) {
            logger.warn("유효하지 않은 JWT 서명: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("JWT 인증 처리 중 오류 발생", e);
        } finally {
            // 필터 체인 진행 전에 인증 실패 시 SecurityContext 정리
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                SecurityContextHolder.clearContext();
            }
        }

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

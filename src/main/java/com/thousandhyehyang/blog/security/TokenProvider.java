package com.thousandhyehyang.blog.security;

import com.thousandhyehyang.blog.config.JwtProperties;
import com.thousandhyehyang.blog.entity.Account;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class TokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    private final JwtProperties jwtProperties;
    private final Key key;

    public TokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getAccessTokenExpirationMs());

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "access")
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getRefreshTokenExpirationMs());

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "refresh")
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return Long.parseLong(claims.getSubject());
        } catch (ExpiredJwtException e) {
            logger.error("만료된 JWT 토큰에서 사용자 ID 추출 시도", e);
            throw e;
        } catch (Exception e) {
            logger.error("JWT 토큰에서 사용자 ID 추출 중 오류 발생", e);
            throw e;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            logger.error("Invalid JWT signature", e);
        } catch (ExpiredJwtException e) {
            logger.error("Expired JWT token", e);
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token", e);
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty", e);
        }
        return false;
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return "refresh".equals(claims.get("type"));
        } catch (ExpiredJwtException e) {
            logger.warn("만료된 JWT 토큰으로 리프레시 토큰 확인 시도", e);
            return false;
        } catch (MalformedJwtException e) {
            logger.warn("잘못된 형식의 JWT 토큰으로 리프레시 토큰 확인 시도", e);
            return false;
        } catch (SignatureException e) {
            logger.warn("유효하지 않은 JWT 서명으로 리프레시 토큰 확인 시도", e);
            return false;
        } catch (Exception e) {
            logger.error("JWT 토큰 타입 확인 중 오류 발생", e);
            return false;
        }
    }

    public long getExpirationTime(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getExpiration().getTime();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰의 경우에도 만료 시간을 반환할 수 있음
            return e.getClaims().getExpiration().getTime();
        } catch (Exception e) {
            logger.error("JWT 토큰에서 만료 시간 추출 중 오류 발생", e);
            throw e;
        }
    }
}

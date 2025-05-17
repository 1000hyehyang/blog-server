package com.thousandhyehyang.blog.security;

import com.thousandhyehyang.blog.config.JwtProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;
    private final TokenProvider tokenProvider;

    public RedisTokenService(RedisTemplate<String, String> redisTemplate, JwtProperties jwtProperties, TokenProvider tokenProvider) {
        this.redisTemplate = redisTemplate;
        this.jwtProperties = jwtProperties;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Stores a refresh token in Redis with the user ID as the key
     * @param userId The user ID
     * @param refreshToken The refresh token
     */
    public void storeRefreshToken(Long userId, String refreshToken) {
        String key = getKey(userId);
        long expirationTimeInSeconds = jwtProperties.getRefreshTokenExpirationMs() / 1000;
        redisTemplate.opsForValue().set(key, refreshToken, expirationTimeInSeconds, TimeUnit.SECONDS);
    }

    /**
     * Retrieves a refresh token from Redis
     * @param userId The user ID
     * @return The refresh token, or null if not found
     */
    public String getRefreshToken(Long userId) {
        String key = getKey(userId);
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Deletes a refresh token from Redis
     * @param userId The user ID
     */
    public void deleteRefreshToken(Long userId) {
        String key = getKey(userId);
        redisTemplate.delete(key);
    }

    /**
     * Validates a refresh token against the one stored in Redis
     * @param userId The user ID
     * @param refreshToken The refresh token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateRefreshToken(Long userId, String refreshToken) {
        String storedToken = getRefreshToken(userId);
        return storedToken != null && storedToken.equals(refreshToken) && tokenProvider.validateToken(refreshToken);
    }

    /**
     * Generates the Redis key for a user ID
     * @param userId The user ID
     * @return The Redis key
     */
    private String getKey(Long userId) {
        return "refresh_token:" + userId;
    }
}
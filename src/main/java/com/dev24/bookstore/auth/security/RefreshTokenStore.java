package com.dev24.bookstore.auth.security;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.dev24.bookstore.auth.domain.Role;

@Component
public class RefreshTokenStore {

    private static final String KEY_PREFIX = "refresh-token:";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final StringRedisTemplate redisTemplate;
    private final long refreshTokenExpirationMs;

    public RefreshTokenStore(
            StringRedisTemplate redisTemplate,
            @Value("${app.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.redisTemplate = redisTemplate;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String issue(String loginId, Role role) {
        String refreshToken = generateOpaqueToken(); // SecureRandom 32바이트 → Base64URL
        String value = role.name() + ":" + loginId;
        // Redis에 저장
        redisTemplate.opsForValue().set(KEY_PREFIX + refreshToken, value, Duration.ofMillis(refreshTokenExpirationMs));
        return refreshToken;
    }

    public Optional<RefreshTokenPayload> find(String refreshToken) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + refreshToken);
        if (value == null) {
            return Optional.empty();
        }
        String[] parts = value.split(":", 2);
        return Optional.of(new RefreshTokenPayload(Role.valueOf(parts[0]), parts[1]));
    }

    public void revoke(String refreshToken) {
        redisTemplate.delete(KEY_PREFIX + refreshToken);
    }

    // SecureRandom 기반 랜덤 토큰 생성
    private String generateOpaqueToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

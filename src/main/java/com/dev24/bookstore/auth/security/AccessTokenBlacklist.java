package com.dev24.bookstore.auth.security;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccessTokenBlacklist {

    private static final String KEY_PREFIX = "blacklist:";
    private static final String BLACKLISTED_VALUE = "true"; // 임의 값. 뭘 넣어도 무관함.

    private final StringRedisTemplate redisTemplate;

    public void blacklist(String jti, Duration ttl) {
        if (ttl.isZero() || ttl.isNegative()) { // 이미 만료된 토큰은 저장할 필요 없음
            return;
        }
        redisTemplate.opsForValue().set(KEY_PREFIX + jti, BLACKLISTED_VALUE, ttl);
    }

    public boolean isBlacklisted(String jti) {
        return redisTemplate.hasKey(KEY_PREFIX + jti);
    }
}

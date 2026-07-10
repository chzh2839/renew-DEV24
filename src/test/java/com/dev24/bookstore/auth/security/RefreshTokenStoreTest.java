package com.dev24.bookstore.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.dev24.bookstore.auth.domain.Role;

@ExtendWith(MockitoExtension.class)
class RefreshTokenStoreTest {

    private static final long EXPIRATION_MS = 1_209_600_000L;

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private RefreshTokenStore refreshTokenStore;

    @BeforeEach
    void setUp() {
        refreshTokenStore = new RefreshTokenStore(redisTemplate, EXPIRATION_MS);
    }

    // 발급 시 Redis에 "refresh-token:{토큰}" 키로 "ROLE:loginId" 값이 TTL과 함께 저장되는지 검증
    @Test
    void issue_storesTokenWithRoleAndLoginId() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        String token = refreshTokenStore.issue("dev24", Role.CUSTOMER);

        assertThat(token).isNotBlank();
        verify(valueOperations).set(eq("refresh-token:" + token), eq("CUSTOMER:dev24"), eq(Duration.ofMillis(EXPIRATION_MS)));
    }

    // 같은 사용자라도 발급할 때마다 서로 다른 랜덤 토큰이 생성되는지 검증
    @Test
    void issue_generatesDifferentTokensEachTime() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        String first = refreshTokenStore.issue("dev24", Role.CUSTOMER);
        String second = refreshTokenStore.issue("dev24", Role.CUSTOMER);

        assertThat(first).isNotEqualTo(second);
    }

    // Redis에 저장된 "ROLE:loginId" 값을 RefreshTokenPayload로 올바르게 복원하는지 검증
    @Test
    void find_existingToken_returnsPayload() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("refresh-token:abc")).willReturn("ADMIN:admin01");

        Optional<RefreshTokenPayload> payload = refreshTokenStore.find("abc");

        assertThat(payload).contains(new RefreshTokenPayload(Role.ADMIN, "admin01"));
    }

    // Redis에 없는(만료됐거나 존재한 적 없는) 토큰을 조회하면 빈 Optional을 반환하는지 검증
    @Test
    void find_missingToken_returnsEmpty() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("refresh-token:missing")).willReturn(null);

        Optional<RefreshTokenPayload> payload = refreshTokenStore.find("missing");

        assertThat(payload).isEmpty();
    }

    // revoke 호출 시 해당 리프레시 토큰의 Redis 키가 삭제되는지 검증(로그아웃 시 사용)
    @Test
    void revoke_deletesKey() {
        refreshTokenStore.revoke("abc");

        verify(redisTemplate).delete("refresh-token:abc");
    }
}

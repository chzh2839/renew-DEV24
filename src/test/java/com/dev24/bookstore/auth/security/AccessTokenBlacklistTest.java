package com.dev24.bookstore.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class AccessTokenBlacklistTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private AccessTokenBlacklist accessTokenBlacklist;

    @BeforeEach
    void setUp() {
        accessTokenBlacklist = new AccessTokenBlacklist(redisTemplate);
    }

    // TTL이 양수면 "blacklist:{jti}" 키로 Redis에 저장되는지 검증
    @Test
    void blacklist_positiveTtl_storesKey() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        accessTokenBlacklist.blacklist("some-jti", Duration.ofMinutes(5));

        verify(valueOperations).set("blacklist:some-jti", "true", Duration.ofMinutes(5));
    }

    // TTL이 0 이하(이미 만료된 토큰)면 Redis에 저장할 필요가 없어 아무 호출도 하지 않는지 검증
    @Test
    void blacklist_zeroOrNegativeTtl_doesNotStore() {
        accessTokenBlacklist.blacklist("expired-jti", Duration.ZERO);
        accessTokenBlacklist.blacklist("already-expired-jti", Duration.ofSeconds(-1));

        verifyNoInteractions(redisTemplate);
    }

    // Redis에 해당 jti 키가 존재하면 블랙리스트에 있는 것으로 판단하는지 검증
    @Test
    void isBlacklisted_keyExists_returnsTrue() {
        given(redisTemplate.hasKey("blacklist:jti-a")).willReturn(true);

        assertThat(accessTokenBlacklist.isBlacklisted("jti-a")).isTrue();
    }

    // Redis에 해당 jti 키가 없으면 블랙리스트에 없는 것으로 판단하는지 검증
    @Test
    void isBlacklisted_keyMissing_returnsFalse() {
        given(redisTemplate.hasKey("blacklist:jti-b")).willReturn(false);

        assertThat(accessTokenBlacklist.isBlacklisted("jti-b")).isFalse();
    }
}

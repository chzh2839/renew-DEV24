package com.dev24.bookstore.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dev24.bookstore.auth.domain.Role;

class JwtTokenProviderTest {

    private static final String SECRET = "LeTe/85XGR4dJPP8JaiVPxZMl3tyDkbfIAMaNSMrVAw=";

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, 3600000L);
    }

    // 정상 토큰을 파싱하면 loginId/role/jti/expiration 클레임이 전부 올바르게 채워지는지 검증
    @Test
    void parse_validToken_containsLoginIdRoleJtiAndExpiration() {
        long beforeIssue = System.currentTimeMillis();
        String token = jwtTokenProvider.generateAccessToken("dev24", Role.CUSTOMER);

        AccessTokenClaims claims = jwtTokenProvider.parse(token).orElseThrow();

        assertThat(claims.loginId()).isEqualTo("dev24");
        assertThat(claims.role()).isEqualTo(Role.CUSTOMER);
        assertThat(claims.jti()).isNotBlank();
        assertThat(claims.expiration().getTime()).isCloseTo(beforeIssue + 3600000L, org.assertj.core.data.Offset.offset(5000L));
    }

    // ADMIN 역할로 발급한 토큰도 role 클레임에 ADMIN이 올바르게 담기는지 검증
    @Test
    void parse_forAdmin_containsAdminRole() {
        String token = jwtTokenProvider.generateAccessToken("admin01", Role.ADMIN);

        assertThat(jwtTokenProvider.parse(token).orElseThrow().role()).isEqualTo(Role.ADMIN);
    }

    // 매번 발급되는 토큰의 jti가 서로 겹치지 않고 고유한지 검증(블랙리스트 키로 쓰이므로 중요)
    @Test
    void generateAccessToken_containsUniqueJti() {
        String token1 = jwtTokenProvider.generateAccessToken("dev24", Role.CUSTOMER);
        String token2 = jwtTokenProvider.generateAccessToken("dev24", Role.CUSTOMER);

        String jti1 = jwtTokenProvider.parse(token1).orElseThrow().jti();
        String jti2 = jwtTokenProvider.parse(token2).orElseThrow().jti();

        assertThat(jti1).isNotEqualTo(jti2);
    }

    // 서명 뒤에 문자열을 덧붙여 위변조한 토큰은 서명 검증에 실패해 빈 Optional을 반환하는지 검증
    @Test
    void parse_tamperedToken_returnsEmpty() {
        String token = jwtTokenProvider.generateAccessToken("dev24", Role.ADMIN);

        assertThat(jwtTokenProvider.parse(token + "tampered")).isEmpty();
    }

    // JWT 형식 자체가 아닌 임의 문자열을 넘기면 예외 없이 빈 Optional을 반환하는지 검증
    @Test
    void parse_garbageString_returnsEmpty() {
        assertThat(jwtTokenProvider.parse("not-a-jwt")).isEmpty();
    }

    // TTL이 지나 자연 만료된 토큰은 빈 Optional을 반환하는지 검증
    @Test
    void parse_expiredToken_returnsEmpty() throws InterruptedException {
        JwtTokenProvider shortLived = new JwtTokenProvider(SECRET, 1L);
        String token = shortLived.generateAccessToken("dev24", Role.CUSTOMER);

        Thread.sleep(10);

        assertThat(shortLived.parse(token)).isEmpty();
    }

    // 다른 시크릿으로 서명된 토큰은 우리 secretKey로 검증했을 때 빈 Optional을 반환하는지 검증
    @Test
    void parse_signedWithDifferentSecret_returnsEmpty() {
        JwtTokenProvider otherIssuer = new JwtTokenProvider("VPJ4WJ0scE7N8aR9Bf4GfyF762Xd21/4WLjxQ4h/h+s=", 3600000L);
        String token = otherIssuer.generateAccessToken("dev24", Role.CUSTOMER);

        assertThat(jwtTokenProvider.parse(token)).isEmpty();
    }
}

package com.dev24.bookstore.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;

class BearerTokenResolverTest {

    // 정상적인 "Bearer {token}" 헤더에서 접두사를 떼고 순수 토큰 문자열만 추출하는지 검증 -
    // JwtAuthenticationFilter/AuthController.logout()이 이 결과를 그대로 JwtTokenProvider에 넘긴다.
    @Test
    void resolve_bearerHeader_extractsToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getHeader("Authorization")).willReturn("Bearer abc.def.ghi");

        assertThat(BearerTokenResolver.resolve(request)).isEqualTo("abc.def.ghi");
    }

    // Authorization 헤더 자체가 없는 요청(비로그인 등)은 예외 없이 null을 반환해야 필터가
    // "인증 정보 없음"으로 안전하게 처리할 수 있다.
    @Test
    void resolve_noAuthorizationHeader_returnsNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getHeader("Authorization")).willReturn(null);

        assertThat(BearerTokenResolver.resolve(request)).isNull();
    }

    // "Bearer " 접두사가 없는 다른 인증 스킴(Basic 등)의 헤더는 토큰으로 오인해 추출하면 안 되고 null이어야 한다.
    @Test
    void resolve_headerWithoutBearerPrefix_returnsNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getHeader("Authorization")).willReturn("Basic dXNlcjpwYXNz");

        assertThat(BearerTokenResolver.resolve(request)).isNull();
    }
}

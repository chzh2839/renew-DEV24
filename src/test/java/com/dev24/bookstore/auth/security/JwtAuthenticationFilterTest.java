package com.dev24.bookstore.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.dev24.bookstore.auth.domain.Role;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AccessTokenBlacklist accessTokenBlacklist;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenProvider, accessTokenBlacklist);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // 유효하고 블랙리스트에 없는 토큰이면 SecurityContext에 loginId/역할 권한이 세팅되는지 검증
    @Test
    void doFilter_validToken_setsAuthenticationWithRole() throws Exception {
        given(request.getHeader("Authorization")).willReturn("Bearer valid-token");
        given(jwtTokenProvider.parse("valid-token"))
                .willReturn(Optional.of(new AccessTokenClaims("dev24", Role.CUSTOMER, "jti-1", null)));
        given(accessTokenBlacklist.isBlacklisted("jti-1")).willReturn(false);

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("dev24");
        assertThat(authentication.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_CUSTOMER");
        verify(filterChain).doFilter(request, response);
    }

    // 서명은 유효해도 jti가 블랙리스트에 있으면(로그아웃된 토큰) 인증을 세팅하지 않는지 검증
    @Test
    void doFilter_blacklistedToken_doesNotSetAuthentication() throws Exception {
        given(request.getHeader("Authorization")).willReturn("Bearer logged-out-token");
        given(jwtTokenProvider.parse("logged-out-token"))
                .willReturn(Optional.of(new AccessTokenClaims("dev24", Role.CUSTOMER, "jti-2", null)));
        given(accessTokenBlacklist.isBlacklisted("jti-2")).willReturn(true);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    // Authorization 헤더 자체가 없으면 인증을 세팅하지 않고 그냥 통과시키는지 검증
    @Test
    void doFilter_noAuthorizationHeader_doesNotSetAuthentication() throws Exception {
        given(request.getHeader("Authorization")).willReturn(null);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    // 파싱/검증에 실패한 토큰(parse가 빈 Optional 반환)이면 인증을 세팅하지 않는지 검증
    @Test
    void doFilter_invalidToken_doesNotSetAuthentication() throws Exception {
        given(request.getHeader("Authorization")).willReturn("Bearer invalid-token");
        given(jwtTokenProvider.parse("invalid-token")).willReturn(Optional.empty());

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    // Authorization 헤더가 있어도 Bearer 스킴이 아니면(예: Basic) 토큰으로 취급하지 않는지 검증
    @Test
    void doFilter_nonBearerHeader_doesNotSetAuthentication() throws Exception {
        given(request.getHeader("Authorization")).willReturn("Basic dXNlcjpwYXNz");

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
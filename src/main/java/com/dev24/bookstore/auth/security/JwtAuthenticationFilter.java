package com.dev24.bookstore.auth.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final AccessTokenBlacklist accessTokenBlacklist;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = BearerTokenResolver.resolve(request);
        if (token != null) {
            Optional<AccessTokenClaims> claims = jwtTokenProvider.parse(token) // 서명 검증 + 클레임 추출을 한 번에
                    .filter(c -> !accessTokenBlacklist.isBlacklisted(c.jti()));
            claims.ifPresent(c -> {
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + c.role().name()));
                Authentication authentication = new UsernamePasswordAuthenticationToken(c.loginId(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication); // 인증 정보 세팅
            });
        }
        filterChain.doFilter(request, response);
    }
}

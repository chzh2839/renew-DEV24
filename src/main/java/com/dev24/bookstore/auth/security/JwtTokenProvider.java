package com.dev24.bookstore.auth.security;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dev24.bookstore.auth.domain.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    private static final String ROLE_CLAIM = "role";

    private final SecretKey secretKey;
    private final long accessTokenExpirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration-ms}") long accessTokenExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    public String generateAccessToken(String loginId, Role role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);
        return Jwts.builder()
                .id(UUID.randomUUID().toString())  // jti - 로그아웃 시 블랙리스트 키로 사용
                .subject(loginId)
                .claim(ROLE_CLAIM, role.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 서명 검증 + 클레임 추출을 한 번에 한다. 서명이 틀렸거나 만료됐거나 형식이 깨진 토큰이면 빈 Optional을 반환한다.
     */
    public Optional<AccessTokenClaims> parse(String token) {
        try {
            Claims claims = parseClaims(token);
            return Optional.of(new AccessTokenClaims(
                    claims.getSubject(),
                    Role.valueOf(claims.get(ROLE_CLAIM, String.class)),
                    claims.getId(),
                    claims.getExpiration()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

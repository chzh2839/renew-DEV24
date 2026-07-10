package com.dev24.bookstore.auth.security;

import java.util.Date;

import com.dev24.bookstore.auth.domain.Role;

public record AccessTokenClaims(String loginId, Role role, String jti, Date expiration) {
}

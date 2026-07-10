package com.dev24.bookstore.auth.security;

import com.dev24.bookstore.auth.domain.Role;

public record RefreshTokenPayload(Role role, String loginId) {
}

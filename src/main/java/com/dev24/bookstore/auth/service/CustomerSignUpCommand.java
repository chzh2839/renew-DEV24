package com.dev24.bookstore.auth.service;

public record CustomerSignUpCommand(
        String loginId,
        String rawPassword,
        String name,
        String nickname,
        String email,
        String phone,
        String address,
        String interest,
        boolean newsletterYn) {
}

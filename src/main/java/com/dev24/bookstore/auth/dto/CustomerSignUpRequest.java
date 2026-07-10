package com.dev24.bookstore.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerSignUpRequest(
        @NotBlank @Size(min = 4, max = 50) String loginId,

        // BCrypt는 72바이트를 넘는 부분은 조용히 무시하는데, Spring Security의 BCryptPasswordEncoder는
        // 그 길이를 넘으면 IllegalArgumentException을 던진다. 여기서 미리 막아 500 대신 깔끔한 400으로 처리한다.
        @NotBlank @Size(min = 8, max = 72) String password,

        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 50) String nickname,
        @NotBlank @Email @Size(max = 100) String email,
        @Size(max = 20) String phone,
        @Size(max = 255) String address,
        @Size(max = 255) String interest,
        boolean newsletterYn) {
}

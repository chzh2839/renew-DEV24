package com.dev24.bookstore.auth.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerSignUpRequest(
        @NotBlank @Size(min = 4, max = 50) String loginId,

        // BCrypt는 72바이트를 넘는 부분은 조용히 무시하는데, Spring Security의 BCryptPasswordEncoder는
        // 그 길이를 넘으면 IllegalArgumentException을 던진다. 여기서 미리 막아 500 대신 깔끔한 400으로 처리한다.
        @NotBlank @Size(min = 8, max = 72) String password,

        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 50) String nickname,
        @NotBlank @Email @Size(max = 100) String email,

        // phone 자체는 선택 입력(가입 시 필수 아님)이라 @NotBlank는 안 붙이지만, 값이 있으면 형식은 맞아야 한다.
        @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$", message = "휴대폰 번호 형식이 올바르지 않습니다 (예: 010-1234-5678)")
        String phone,
        @Size(max = 255) String address,
        @Size(max = 255) String interest,
        boolean newsletterYn) {
}

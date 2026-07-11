package com.dev24.bookstore.auth.controller.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String loginId,
        @NotBlank String password) {
}

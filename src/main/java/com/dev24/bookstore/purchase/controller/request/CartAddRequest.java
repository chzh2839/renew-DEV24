package com.dev24.bookstore.purchase.controller.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartAddRequest(
        @NotNull Long bookId,
        @Positive int quantity) {
}

package com.dev24.bookstore.book.controller.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import com.dev24.bookstore.book.domain.BookStatus;

public record BookUpdateRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 100) String authors,
        @Size(max = 100) String publisher,
        LocalDate publishedAt,
        @PositiveOrZero int price,
        String contents,
        String authorInfo,
        @Size(max = 50) String category,
        @NotNull BookStatus status) {
}

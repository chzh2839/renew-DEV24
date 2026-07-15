package com.dev24.bookstore.review.controller.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.dev24.bookstore.review.domain.ReviewType;

public record ReviewUpdateRequest(
        @Min(1) @Max(5) int score,
        @NotBlank @Size(max = 2000) String content,
        @NotNull ReviewType type,
        @Size(max = 255) String imageUrl) {
}

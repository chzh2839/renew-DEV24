package com.dev24.bookstore.review.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PresignedUploadRequest(
        @NotBlank
        @Pattern(regexp = "(?i).+\\.(jpg|jpeg|png|gif|webp)$",
                message = "이미지 파일(jpg/jpeg/png/gif/webp)만 업로드할 수 있습니다")
        String fileName) {
}

package com.dev24.bookstore.review.controller.response;

public record PresignedUploadResponse(String uploadUrl, String objectKey) {
}

package com.dev24.bookstore.review.controller.response;

import java.time.LocalDateTime;

import com.dev24.bookstore.review.domain.Review;
import com.dev24.bookstore.review.domain.ReviewType;

public record ReviewResponse(
        Long id,
        Long bookId,
        Long customerId,
        int score,
        String content,
        ReviewType type,
        String imageUrl,
        LocalDateTime writtenAt) {

    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getBook().getId(),
                review.getCustomer().getId(),
                review.getScore(),
                review.getContent(),
                review.getType(),
                review.getImageUrl(),
                review.getWrittenAt());
    }
}

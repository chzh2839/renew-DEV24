package com.dev24.bookstore.review.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.dev24.bookstore.review.controller.response.ReviewResponse;
import com.dev24.bookstore.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewQueryService {

    private final ReviewRepository reviewRepository;

    public Page<ReviewResponse> getReviews(Long bookId, Pageable pageable) {
        return reviewRepository.findAllByBookId(bookId, pageable).map(ReviewResponse::from);
    }
}

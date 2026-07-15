package com.dev24.bookstore.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev24.bookstore.review.domain.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}

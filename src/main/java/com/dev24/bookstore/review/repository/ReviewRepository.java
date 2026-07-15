package com.dev24.bookstore.review.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.dev24.bookstore.review.domain.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findAllByBookId(Long bookId, Pageable pageable);

    boolean existsByImageUrl(String imageUrl);
}

package com.dev24.bookstore.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev24.bookstore.book.domain.Rating;

public interface RatingRepository extends JpaRepository<Rating, Long> {
}

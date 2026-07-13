package com.dev24.bookstore.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev24.bookstore.book.domain.BookImage;

public interface BookImageRepository extends JpaRepository<BookImage, Long> {
}

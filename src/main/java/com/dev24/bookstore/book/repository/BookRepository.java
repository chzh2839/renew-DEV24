package com.dev24.bookstore.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev24.bookstore.book.domain.Book;

public interface BookRepository extends JpaRepository<Book, Long>, BookQueryRepository {

    boolean existsByIsbn(String isbn);
}

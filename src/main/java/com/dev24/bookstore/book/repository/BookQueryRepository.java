package com.dev24.bookstore.book.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.dev24.bookstore.book.domain.Book;

public interface BookQueryRepository {

    Page<Book> search(BookSearchCondition condition, Pageable pageable);
}

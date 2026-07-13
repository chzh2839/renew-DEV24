package com.dev24.bookstore.book.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.repository.BookRepository;
import com.dev24.bookstore.book.repository.BookSearchCondition;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookQueryService {

    private final BookRepository bookRepository;

    public Page<Book> search(BookSearchCondition condition, Pageable pageable) {
        return bookRepository.search(condition, pageable);
    }
}

package com.dev24.bookstore.book.controller.response;

import java.time.LocalDate;

import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookStatus;

public record BookResponse(
        Long id,
        String isbn,
        String title,
        String authors,
        String publisher,
        LocalDate publishedAt,
        int price,
        String category,
        BookStatus status) {

    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(), book.getIsbn(), book.getTitle(), book.getAuthors(), book.getPublisher(),
                book.getPublishedAt(), book.getPrice(), book.getCategory(), book.getStatus());
    }
}

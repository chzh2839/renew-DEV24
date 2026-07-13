package com.dev24.bookstore.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.book.repository.BookRepository;
import com.dev24.bookstore.book.repository.BookSearchCondition;

@ExtendWith(MockitoExtension.class)
class BookQueryServiceTest {

    @Mock
    private BookRepository bookRepository;

    private BookQueryService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookQueryService(bookRepository);
    }

    // BookQueryService.search가 조건/페이징 정보를 그대로 BookRepository.search에 위임하는지 검증
    @Test
    void search_delegatesToRepository() {
        Book book = new Book("9788983920774", "자바의 정석", "남궁성", "도우출판", null, 10000, "내용", null, "프로그래밍",
                BookStatus.ACTIVE);
        BookSearchCondition condition = new BookSearchCondition("자바", "프로그래밍", BookStatus.ACTIVE);
        PageRequest pageable = PageRequest.of(0, 20);
        Page<Book> expected = new PageImpl<>(List.of(book), pageable, 1);
        given(bookRepository.search(condition, pageable)).willReturn(expected);

        Page<Book> result = bookService.search(condition, pageable);

        assertThat(result).isSameAs(expected);
    }
}

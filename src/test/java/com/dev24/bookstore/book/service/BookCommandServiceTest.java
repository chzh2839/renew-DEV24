package com.dev24.bookstore.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dev24.bookstore.book.controller.request.BookUpdateRequest;
import com.dev24.bookstore.book.controller.response.BookResponse;
import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.book.repository.BookRepository;
import com.dev24.bookstore.common.exception.BusinessException;
import com.dev24.bookstore.common.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class BookCommandServiceTest {

    @Mock
    private BookRepository bookRepository;

    private BookCommandService bookCommandService;

    @BeforeEach
    void setUp() {
        bookCommandService = new BookCommandService(bookRepository);
    }

    private Book book(String isbn, String title) {
        return new Book(isbn, title, "남궁성", "도우출판", null, 10000, "내용", null, "프로그래밍", BookStatus.ACTIVE);
    }

    // 존재하는 id면 엔티티를 요청값으로 갱신하고 BookResponse로 매핑해서 반환하는지 검증
    @Test
    void update_existingId_updatesAndReturnsBookResponse() {
        Book book = book("9788983920774", "자바의 정석");
        given(bookRepository.findByIdWithDetails(1L)).willReturn(Optional.of(book));
        BookUpdateRequest request = new BookUpdateRequest(
                "자바의 정석 3판", "남궁성", "도우출판", LocalDate.of(2024, 1, 1),
                20000, "개정 내용", "저자 소개", "IT전문서", BookStatus.OUT_OF_PRINT);

        BookResponse result = bookCommandService.update(1L, request);

        assertThat(result.title()).isEqualTo("자바의 정석 3판");
        assertThat(result.price()).isEqualTo(20000);
        assertThat(result.status()).isEqualTo(BookStatus.OUT_OF_PRINT);
        assertThat(book.getTitle()).isEqualTo("자바의 정석 3판");
    }

    // 존재하지 않는 id면 BusinessException(ENTITY_NOT_FOUND)을 던지는지 검증
    @Test
    void update_missingId_throwsEntityNotFound() {
        given(bookRepository.findByIdWithDetails(999L)).willReturn(Optional.empty());
        BookUpdateRequest request = new BookUpdateRequest(
                "제목", null, null, null, 0, null, null, null, BookStatus.ACTIVE);

        assertThatThrownBy(() -> bookCommandService.update(999L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }
}

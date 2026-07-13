package com.dev24.bookstore.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.dev24.bookstore.book.controller.response.BookResponse;
import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.book.repository.BookRepository;
import com.dev24.bookstore.book.repository.BookSearchCondition;
import com.dev24.bookstore.common.exception.BusinessException;
import com.dev24.bookstore.common.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class BookQueryServiceTest {

    @Mock
    private BookRepository bookRepository;

    private BookQueryService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookQueryService(bookRepository);
    }

    private Book book(String isbn, String title) {
        return new Book(isbn, title, "남궁성", "도우출판", null, 10000, "내용", null, "프로그래밍", BookStatus.ACTIVE);
    }

    // search()가 리포지토리 결과(Page<Book> 엔티티)를 캐시 가능한 경량 DTO(BookSearchResult)로 매핑해서
    // 반환하는지 검증 - 엔티티가 아니라 이 DTO가 @Cacheable의 캐시 값이 된다.
    @Test
    void search_mapsRepositoryPageToCacheableResult() {
        Book book = book("9788983920774", "자바의 정석");
        BookSearchCondition condition = new BookSearchCondition("자바", "프로그래밍", BookStatus.ACTIVE);
        PageRequest pageable = PageRequest.of(0, 20);
        Page<Book> repositoryPage = new PageImpl<>(List.of(book), pageable, 1);
        given(bookRepository.search(condition, pageable)).willReturn(repositoryPage);

        BookSearchResult result = bookService.search(condition, pageable);

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).title()).isEqualTo("자바의 정석");
        assertThat(result.content().get(0).isbn()).isEqualTo("9788983920774");
    }

    // 존재하는 id면 fetch join 리포지토리 메서드 결과를 BookResponse로 매핑해서 반환하는지 검증
    @Test
    void getDetail_existingId_returnsBookResponse() {
        given(bookRepository.findByIdWithDetails(1L))
                .willReturn(Optional.of(book("9788983920774", "자바의 정석")));

        BookResponse result = bookService.getDetail(1L);

        assertThat(result.title()).isEqualTo("자바의 정석");
    }

    // 존재하지 않는 id면 BusinessException(ENTITY_NOT_FOUND)을 던지는지 검증
    // - @Cacheable은 예외 발생 시, 캐시에 아무것도 저장하지 않으므로 "없음"이라는 결과 자체가 캐시에 남지 않는다.
    @Test
    void getDetail_missingId_throwsEntityNotFound() {
        given(bookRepository.findByIdWithDetails(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getDetail(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }
}

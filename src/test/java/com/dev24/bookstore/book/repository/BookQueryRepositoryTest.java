package com.dev24.bookstore.book.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.common.config.QuerydslConfig;

// 실제 Postgres(Flyway 마이그레이션 포함)에 QueryDSL로 동적 검색/필터 + Pageable 페이징이
// 레거시 rownum 방식을 대체해 올바르게 동작하는지 검증한다.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(QuerydslConfig.class)
@Testcontainers
class BookQueryRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private BookRepository bookRepository;

    private Book book(String isbn, String title, String authors, String publisher, String category, BookStatus status) {
        return new Book(isbn, title, authors, publisher, null, 10000, "내용 " + title, null, category, status);
    }

    // 필터 없이 전체 조회 시 전체 건수와 페이지 크기가 올바른지 검증
    @Test
    void search_noFilter_returnsAllBooksPaged() {
        bookRepository.save(book("1000000000001", "자바의 정석", "남궁성", "도우출판", "프로그래밍", BookStatus.ACTIVE));
        bookRepository.save(book("1000000000002", "이펙티브 자바", "조슈아 블로크", "인사이트", "프로그래밍", BookStatus.ACTIVE));
        bookRepository.save(book("1000000000003", "채식주의자", "한강", "창비", "소설", BookStatus.ACTIVE));

        Page<Book> page = bookRepository.search(
                new BookSearchCondition(null, null, null), PageRequest.of(0, 2, Sort.by("id")));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    // keyword가 title/authors/publisher/contents 중 하나에라도 대소문자 무시로 매칭되는지 검증
    @Test
    void search_byKeyword_matchesAcrossMultipleFields() {
        bookRepository.save(book("1000000000004", "자바의 정석", "남궁성", "도우출판", "프로그래밍", BookStatus.ACTIVE));
        bookRepository.save(book("1000000000005", "이펙티브 자바", "조슈아 블로크", "인사이트", "프로그래밍", BookStatus.ACTIVE));
        bookRepository.save(book("1000000000006", "채식주의자", "한강", "창비", "소설", BookStatus.ACTIVE));

        Page<Book> page = bookRepository.search(
                new BookSearchCondition("자바", null, null), PageRequest.of(0, 10, Sort.by("id")));

        assertThat(page.getContent()).extracting(Book::getTitle)
                .containsExactlyInAnyOrder("자바의 정석", "이펙티브 자바");
    }

    // category와 status를 함께 조합했을 때 정확히 일치하는 항목만 필터링되는지 검증
    @Test
    void search_byCategoryAndStatus_filtersExactMatchOnly() {
        bookRepository.save(book("1000000000007", "자바의 정석", "남궁성", "도우출판", "프로그래밍", BookStatus.ACTIVE));
        bookRepository.save(book("1000000000008", "절판된 프로그래밍책", "저자", "출판사", "프로그래밍", BookStatus.OUT_OF_PRINT));
        bookRepository.save(book("1000000000009", "채식주의자", "한강", "창비", "소설", BookStatus.ACTIVE));

        Page<Book> page = bookRepository.search(
                new BookSearchCondition(null, "프로그래밍", BookStatus.ACTIVE), PageRequest.of(0, 10, Sort.by("id")));

        assertThat(page.getContent()).extracting(Book::getTitle).containsExactly("자바의 정석");
    }

    // 조건에 맞는 도서가 없으면 빈 페이지가 반환되는지 검증
    @Test
    void search_noMatch_returnsEmptyPage() {
        bookRepository.save(book("1000000000010", "자바의 정석", "남궁성", "도우출판", "프로그래밍", BookStatus.ACTIVE));

        Page<Book> page = bookRepository.search(
                new BookSearchCondition("존재하지않는키워드", null, null), PageRequest.of(0, 10, Sort.by("id")));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }
}

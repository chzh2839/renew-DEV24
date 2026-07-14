package com.dev24.bookstore.book.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManagerFactory;

import com.dev24.bookstore.book.controller.request.BookUpdateRequest;
import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookImage;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.book.domain.Rating;
import com.dev24.bookstore.book.repository.BookImageRepository;
import com.dev24.bookstore.book.repository.BookRepository;
import com.dev24.bookstore.book.repository.BookSearchCondition;
import com.dev24.bookstore.book.repository.RatingRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Testcontainers
class BookCommandServiceCacheTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private BookQueryService bookQueryService;
    @Autowired
    private BookCommandService bookCommandService;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private BookImageRepository bookImageRepository;
    @Autowired
    private RatingRepository ratingRepository;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics statistics;

    @BeforeEach
    void setUp() {
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
    }

    private BookUpdateRequest updateRequest() {
        return new BookUpdateRequest(
                "캐시 테스트 도서(수정)", "저자", "출판사", LocalDate.of(2024, 2, 1),
                15000, "수정된 내용", null, "프로그래밍", BookStatus.ACTIVE);
    }

    @Test
    void update_evictsBookDetailCache_subsequentGetDetailHitsDatabaseAgain() {
        Book book = bookRepository.save(new Book(
                "9000000000002", "캐시 테스트 도서", "저자", "출판사", LocalDate.of(2024, 1, 15),
                10000, "내용", null, "프로그래밍", BookStatus.ACTIVE));
        bookImageRepository.save(new BookImage(book, "https://example.com/2.jpg"));
        ratingRepository.save(new Rating(book));
        Long id = book.getId();

        bookQueryService.getDetail(id); // 캐시 채우기(미스)
        bookQueryService.getDetail(id); // 캐시 히트 확인용 워밍업

        bookCommandService.update(id, updateRequest());

        statistics.clear();
        bookQueryService.getDetail(id); // 캐시가 비워졌으면 다시 DB를 타야 함

        assertThat(statistics.getQueryExecutionCount()).isGreaterThan(0);
    }

    @Test
    void update_evictsBookSearchCache_subsequentSearchHitsDatabaseAgain() {
        Book book = bookRepository.save(new Book(
                "9000000000003", "캐시 검색 테스트 도서", "저자", "출판사", LocalDate.of(2024, 1, 15),
                10000, "내용", null, "프로그래밍", BookStatus.ACTIVE));
        bookImageRepository.save(new BookImage(book, "https://example.com/3.jpg"));
        ratingRepository.save(new Rating(book));
        Long id = book.getId();
        BookSearchCondition condition = new BookSearchCondition("캐시 검색", null, null);
        Pageable pageable = PageRequest.of(0, 20);

        bookQueryService.search(condition, pageable); // 캐시 채우기(미스)
        bookQueryService.search(condition, pageable); // 캐시 히트 확인용 워밍업

        bookCommandService.update(id, updateRequest());

        statistics.clear();
        bookQueryService.search(condition, pageable); // 캐시가 비워졌으면 다시 DB를 타야 함

        assertThat(statistics.getQueryExecutionCount()).isGreaterThan(0);
    }
}

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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManagerFactory;

import com.dev24.bookstore.book.controller.response.BookResponse;
import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookImage;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.book.domain.Rating;
import com.dev24.bookstore.book.repository.BookImageRepository;
import com.dev24.bookstore.book.repository.BookRepository;
import com.dev24.bookstore.book.repository.RatingRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Testcontainers
class BookQueryServiceCacheTest {

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

    // publishedAt에 실제 LocalDate 값을 넣어, Redis 왕복(직렬화→역직렬화) 후에도 값이 그대로 보존되는지 함께 확인한다
    // - BookCacheConfig의 ObjectMapper에 JavaTimeModule이 실제로 등록돼 동작하는지의 증거.
    @Test
    void getDetail_secondCall_isServedFromRedisWithoutHittingDatabase() {
        Book book = bookRepository.save(new Book(
                "9000000000001", "캐시 테스트 도서", "저자", "출판사", LocalDate.of(2024, 1, 15),
                10000, "내용", null, "프로그래밍", BookStatus.ACTIVE));
        bookImageRepository.save(new BookImage(book, "https://example.com/1.jpg"));
        ratingRepository.save(new Rating(book));
        Long id = book.getId();

        statistics.clear();

        BookResponse first = bookQueryService.getDetail(id);
        long queriesAfterFirstCall = statistics.getQueryExecutionCount();
        assertThat(queriesAfterFirstCall).isGreaterThan(0);

        BookResponse second = bookQueryService.getDetail(id);

        assertThat(statistics.getQueryExecutionCount()).isEqualTo(queriesAfterFirstCall);
        assertThat(second).isEqualTo(first);
    }
}

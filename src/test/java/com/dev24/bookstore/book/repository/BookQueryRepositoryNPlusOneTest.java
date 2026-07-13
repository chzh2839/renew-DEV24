package com.dev24.bookstore.book.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManagerFactory;

import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookImage;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.book.domain.Rating;
import com.dev24.bookstore.common.config.QuerydslConfig;

// book_image/rating을 fetch join 없이 지연 로딩하면 도서 건수만큼 추가 쿼리(N+1)가 발생한다.
// BookQueryRepositoryImpl.search()가 leftJoin(...).fetchJoin()으로 두 연관관계를 한 번의 쿼리에 담아오는지 Hibernate Statistics로 직접 증명한다.
// generate_statistics는 이 테스트 클래스에만 켜고(application.properties는 건드리지 않음) 다른 테스트에는 영향 없다.
@DataJpaTest // JPA 계층만 테스트 - 테스트용 DataSource를 자동으로 인메모리 DB(H2 등)로 바꿈
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // @DataJpaTest에서 H2로 자동 지환 기능 못 하게 막기
@Import(QuerydslConfig.class)
@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true") // Hibernate는 약간의 오버헤드 때문에 기본적으로 쿼리 실행 횟수 등 통계 수집하지 않음.
@Testcontainers
class BookQueryRepositoryNPlusOneTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

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

    // 도서 5건 각각에 이미지/평점을 붙여도, 실행 쿼리 수는 content 조회 1회 + count 조회 1회 = 2회로 고정되어야 한다(건수에 비례해 늘어나면 N+1 회귀).
    // 페이지 크기(3)를 전체 건수(5)보다 작게 잡아야 한다 - PageableExecutionUtils.getPage()는 조회된
    // content 건수가 페이지 크기보다 작으면(=마지막 페이지임을 알 수 있으면) count 쿼리 자체를 생략하는 최적화가 있어서,
    // 전체 건수 이상으로 페이지를 잡으면 애초에 count 쿼리가 안 나가 "쿼리 수가 고정됨"을 제대로 검증하지 못한다.
    @Test
    void search_withFetchJoin_executesFixedQueryCountRegardlessOfRowCount() {
        for (int i = 1; i <= 5; i++) {
            Book book = bookRepository.save(new Book(
                    "90000000000" + i, "도서" + i, "저자" + i, "출판사", null,
                    10000, "내용" + i, null, "프로그래밍", BookStatus.ACTIVE));
            bookImageRepository.save(new BookImage(book, "https://example.com/" + i + ".jpg"));
            ratingRepository.save(new Rating(book));
        }

        // Book/BookImage/Rating은 IDENTITY 전략이라 save() 시점에 즉시 INSERT가 실행되므로,
        // 이 준비 단계의 쿼리는 카운트에서 제외하고 검색 직전에 통계를 초기화한다.
        statistics.clear();

        Page<Book> page = bookRepository.search(
                new BookSearchCondition(null, null, null), PageRequest.of(0, 3, Sort.by("id")));

        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(statistics.getQueryExecutionCount()).isEqualTo(2);
    }
}

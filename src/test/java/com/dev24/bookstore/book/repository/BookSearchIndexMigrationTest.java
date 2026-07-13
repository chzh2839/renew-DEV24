package com.dev24.bookstore.book.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.dev24.bookstore.common.config.QuerydslConfig;

// V5__add_book_search_index.sql이 실제 Postgres에 정상 적용되어 검색용 인덱스가
// 모두 생성되는지 확인한다. 실행계획(Seq Scan/Bitmap Scan 등)은 데이터 볼륨과
// 플래너 통계에 좌우되므로 여기서 단언하지 않고 docs/PERFORMANCE.md에 실측으로 남긴다.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(QuerydslConfig.class)
@Testcontainers
class BookSearchIndexMigrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private DataSource dataSource;

    @Test
    void v5Migration_createsExpectedSearchIndexes() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<String> indexNames = jdbcTemplate.queryForList(
                "SELECT indexname FROM pg_indexes WHERE tablename = 'book'", String.class);

        assertThat(indexNames).contains(
                "idx_book_title_trgm", "idx_book_authors_trgm",
                "idx_book_publisher_trgm", "idx_book_contents_trgm",
                "idx_book_category_status");
    }
}

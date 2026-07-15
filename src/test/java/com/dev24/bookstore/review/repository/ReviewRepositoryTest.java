package com.dev24.bookstore.review.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.dev24.bookstore.auth.domain.Customer;
import com.dev24.bookstore.auth.repository.CustomerRepository;
import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.book.repository.BookRepository;
import com.dev24.bookstore.common.config.QuerydslConfig;
import com.dev24.bookstore.purchase.domain.Purchase;
import com.dev24.bookstore.purchase.domain.PurchaseItem;
import com.dev24.bookstore.purchase.domain.enums.PaymentMethod;
import com.dev24.bookstore.purchase.repository.PurchaseItemRepository;
import com.dev24.bookstore.purchase.repository.PurchaseRepository;
import com.dev24.bookstore.review.domain.Review;
import com.dev24.bookstore.review.domain.ReviewType;

// 실제 Postgres(Flyway 마이그레이션 포함)에 엔티티 매핑/연관관계/제약조건이 올바른지 검증한다.
// BookRepository가 BookQueryRepository(QueryDSL)를 함께 확장하므로 JPAQueryFactory 빈이 필요해 QuerydslConfig를 Import
// (PurchaseModuleRepositoryTest와 동일한 이유).
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(QuerydslConfig.class)
@Testcontainers
class ReviewRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private PurchaseItemRepository purchaseItemRepository;

    private Customer customer(String loginId) {
        return customerRepository.save(new Customer(
                loginId, "encoded-password", "홍길동", "길동이", loginId + "@example.com",
                "010-0000-0000", "서울시 강남구", "소설", false));
    }

    private Book book(String isbn) {
        return bookRepository.save(new Book(
                isbn, "자바의 정석", "남궁성", "도우출판", null, 10000, "내용", null, "프로그래밍", BookStatus.ACTIVE));
    }

    private PurchaseItem purchaseItem(Customer customer, Book book) {
        Purchase purchase = purchaseRepository.save(new Purchase(
                customer, "홍길동", "010-1111-1111", "홍길동", "010-1111-1111",
                "06236", "서울시 강남구", PaymentMethod.CREDIT_CARD, 20000));
        return purchaseItemRepository.save(new PurchaseItem(purchase, book, 2, 20000));
    }

    @Test
    void review_save_persistsWithCustomerBookPurchaseItemRelations() {
        Customer customer = customer("review-owner");
        Book book = book("9000000000401");
        PurchaseItem purchaseItem = purchaseItem(customer, book);

        Review saved = reviewRepository.save(
                new Review(customer, book, purchaseItem, 5, "정말 좋은 책이었습니다", ReviewType.TEXT, null));

        Review found = reviewRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getCustomer().getId()).isEqualTo(customer.getId());
        assertThat(found.getBook().getId()).isEqualTo(book.getId());
        assertThat(found.getPurchaseItem().getId()).isEqualTo(purchaseItem.getId());
        assertThat(found.getScore()).isEqualTo(5);
        assertThat(found.getType()).isEqualTo(ReviewType.TEXT);
        assertThat(found.getWrittenAt()).isNotNull();
    }

    // 구매 인증 리뷰 구조상 구매 아이템 1개당 리뷰는 최대 1개(1:0~1) - 같은 purchase_item_id로 두 번째 리뷰를
    // 저장하면 유니크 제약 위반이 나는지 검증
    @Test
    void review_duplicatePurchaseItem_violatesUniqueConstraint() {
        Customer customer = customer("review-dup-owner");
        Book book = book("9000000000402");
        PurchaseItem purchaseItem = purchaseItem(customer, book);
        reviewRepository.saveAndFlush(
                new Review(customer, book, purchaseItem, 4, "첫 리뷰", ReviewType.TEXT, null));

        assertThatThrownBy(() -> reviewRepository.saveAndFlush(
                new Review(customer, book, purchaseItem, 3, "같은 구매건 재리뷰 시도", ReviewType.TEXT, null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

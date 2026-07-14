package com.dev24.bookstore.purchase.repository;

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

import jakarta.persistence.EntityManager;

import com.dev24.bookstore.auth.domain.Admin;
import com.dev24.bookstore.auth.domain.Customer;
import com.dev24.bookstore.auth.repository.AdminRepository;
import com.dev24.bookstore.auth.repository.CustomerRepository;
import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.book.repository.BookRepository;
import com.dev24.bookstore.common.config.QuerydslConfig;
import com.dev24.bookstore.purchase.domain.Cart;
import com.dev24.bookstore.purchase.domain.Purchase;
import com.dev24.bookstore.purchase.domain.PurchaseItem;
import com.dev24.bookstore.purchase.domain.Stock;
import com.dev24.bookstore.purchase.domain.enums.PaymentMethod;

// 실제 Postgres(Flyway 마이그레이션 포함)에 엔티티 매핑/연관관계/제약조건이 올바른지 검증한다.
// BookRepository가 BookQueryRepository(QueryDSL)를 함께 확장하므로 JPAQueryFactory 빈이 필요해 QuerydslConfig를 Import.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(QuerydslConfig.class)
@Testcontainers
class PurchaseModuleRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private PurchaseItemRepository purchaseItemRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private EntityManager entityManager;

    private Customer customer(String loginId) {
        return customerRepository.save(new Customer(
                loginId, "encoded-password", "홍길동", "길동이", loginId + "@example.com",
                "010-0000-0000", "서울시 강남구", "소설", false));
    }

    private Admin admin(String loginId) {
        return adminRepository.save(new Admin(loginId, "encoded-password", "관리자"));
    }

    private Book book(String isbn) {
        return bookRepository.save(new Book(
                isbn, "자바의 정석", "남궁성", "도우출판", null, 10000, "내용", null, "프로그래밍", BookStatus.ACTIVE));
    }

    @Test
    void cart_save_persistsWithCustomerAndBookRelations() {
        Customer customer = customer("cart-owner");
        Book book = book("9000000000101");

        Cart saved = cartRepository.save(new Cart(customer, book, 2, 20000));

        Cart found = cartRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getCustomer().getId()).isEqualTo(customer.getId());
        assertThat(found.getBook().getId()).isEqualTo(book.getId());
        assertThat(found.getQuantity()).isEqualTo(2);
        assertThat(found.getPriceSnapshot()).isEqualTo(20000);
    }

    // 레거시 addToCart(MERGE)가 book_id만으로 매칭해 다른 고객 장바구니에 병합될 수 있던 버그를
    // (customer_id, book_id) 유니크 제약으로 원천 차단했는지 검증
    @Test
    void cart_duplicateCustomerAndBook_violatesUniqueConstraint() {
        Customer customer = customer("cart-dup-owner");
        Book book = book("9000000000102");
        cartRepository.saveAndFlush(new Cart(customer, book, 1, 10000));

        assertThatThrownBy(() -> cartRepository.saveAndFlush(new Cart(customer, book, 1, 10000)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void purchase_withItems_persistsHeaderAndLines() {
        Customer customer = customer("purchase-owner");
        Book book = book("9000000000103");
        Purchase purchase = purchaseRepository.save(new Purchase(
                customer, "홍길동", "010-1111-1111", "홍길동", "010-1111-1111",
                "06236", "서울시 강남구", PaymentMethod.CREDIT_CARD, 20000));
        purchaseItemRepository.save(new PurchaseItem(purchase, book, 2, 20000));

        // items는 cascade 없는 mappedBy 컬렉션이라 같은 영속성 컨텍스트 안에서는 자동 반영되지 않음 -
        // flush+clear로 1차 캐시를 비우고 DB에서 다시 읽어야 실제 저장 상태를 검증할 수 있다.
        entityManager.flush();
        entityManager.clear();

        Purchase found = purchaseRepository.findById(purchase.getId()).orElseThrow();
        assertThat(found.getItems()).hasSize(1);
        assertThat(found.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(found.getItems().get(0).getOrderState().name()).isEqualTo("PREPARING");
    }

    @Test
    void stock_save_persistsWithBookOneToOneAndVersionStartsAtZero() {
        Admin admin = admin("stock-admin");
        Book book = book("9000000000104");

        Stock saved = stockRepository.save(new Stock(book, admin, 100, 12000, 5));

        Stock found = stockRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getBook().getId()).isEqualTo(book.getId());
        assertThat(found.getVersion()).isEqualTo(0);
    }

    @Test
    void stock_duplicateBook_violatesUniqueConstraint() {
        Admin admin = admin("stock-dup-admin");
        Book book = book("9000000000105");
        stockRepository.saveAndFlush(new Stock(book, admin, 100, 12000, 5));

        assertThatThrownBy(() -> stockRepository.saveAndFlush(new Stock(book, admin, 50, 12000, 5)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

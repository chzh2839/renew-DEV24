package com.dev24.bookstore.purchase.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.dev24.bookstore.auth.domain.Admin;
import com.dev24.bookstore.auth.domain.Customer;
import com.dev24.bookstore.auth.repository.AdminRepository;
import com.dev24.bookstore.auth.repository.CustomerRepository;
import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.book.repository.BookRepository;
import com.dev24.bookstore.common.exception.BusinessException;
import com.dev24.bookstore.common.exception.ErrorCode;
import com.dev24.bookstore.purchase.controller.request.PurchaseRequest;
import com.dev24.bookstore.purchase.controller.response.PurchaseResponse;
import com.dev24.bookstore.purchase.domain.Cart;
import com.dev24.bookstore.purchase.domain.Stock;
import com.dev24.bookstore.purchase.domain.enums.PaymentMethod;
import com.dev24.bookstore.purchase.repository.CartRepository;
import com.dev24.bookstore.purchase.repository.PurchaseRepository;
import com.dev24.bookstore.purchase.repository.StockRepository;

// 실제 Postgres에서 구매 플로우 전체가 하나의 트랜잭션으로 묶여 원자적으로 롤백되는지 검증한다
// (Mockito 단위테스트로는 실제 DB 커밋/롤백을 증명할 수 없어 별도로 둔다).
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class PurchaseCommandServiceTransactionalTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private PurchaseCommandService purchaseCommandService;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;

    private Customer customer(String loginId) {
        return customerRepository.save(new Customer(
                loginId, "encoded", "홍길동", "길동이", loginId + "@example.com",
                "010-0000-0000", "서울시 강남구", "소설", false));
    }

    private Admin admin(String loginId) {
        return adminRepository.save(new Admin(loginId, "encoded", "관리자"));
    }

    private Book book(String isbn) {
        return bookRepository.save(new Book(
                isbn, "자바의 정석", "남궁성", "도우출판", LocalDate.of(2024, 1, 1),
                10000, "내용", null, "프로그래밍", BookStatus.ACTIVE));
    }

    private PurchaseRequest request(List<Long> cartItemIds) {
        return new PurchaseRequest(cartItemIds, "홍길동", "010-1111-1111", "홍길동", "010-1111-1111",
                "06236", "서울시 강남구", PaymentMethod.CREDIT_CARD);
    }

    // 정상 흐름에서 Purchase 저장, Stock 차감, 장바구니 삭제가 실제 DB에 함께 반영되는지 검증
    @Test
    void purchase_success_persistsPurchaseAndDecrementsStockAndDeletesCart() {
        Customer customer = customer("txn-success");
        Admin admin = admin("txn-success-admin");
        Book book = book("9100000000001");
        stockRepository.save(new Stock(book, admin, 10, 12000, 2));
        Cart cartItem = cartRepository.save(new Cart(customer, book, 2, 20000));

        PurchaseResponse response = purchaseCommandService.purchase("txn-success", request(List.of(cartItem.getId())));

        assertThat(response.totalPrice()).isEqualTo(20000);
        assertThat(purchaseRepository.count()).isEqualTo(1);
        assertThat(stockRepository.findByBookId(book.getId()).orElseThrow().getQuantity()).isEqualTo(8);
        assertThat(cartRepository.findById(cartItem.getId())).isEmpty();
    }

    // 장바구니 2건 중 첫 번째는 재고 충분·두 번째는 부족 -> 예외 발생 후 (a) Purchase 미생성 (b) 첫 번째 재고 원복 (c) 장바구니 그대로
    @Test
    void purchase_insufficientStockOnSecondItem_rollsBackEverything() {
        Customer customer = customer("txn-rollback");
        Admin admin = admin("txn-rollback-admin");
        Book sufficientBook = book("9100000000002");
        Book insufficientBook = book("9100000000003");
        stockRepository.save(new Stock(sufficientBook, admin, 10, 12000, 0));
        stockRepository.save(new Stock(insufficientBook, admin, 1, 12000, 0));
        Cart sufficientCartItem = cartRepository.save(new Cart(customer, sufficientBook, 2, 20000));
        Cart insufficientCartItem = cartRepository.save(new Cart(customer, insufficientBook, 5, 50000));

        long purchaseCountBefore = purchaseRepository.count();

        assertThatThrownBy(() -> purchaseCommandService.purchase(
                "txn-rollback", request(List.of(sufficientCartItem.getId(), insufficientCartItem.getId()))))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INSUFFICIENT_STOCK);

        assertThat(purchaseRepository.count()).isEqualTo(purchaseCountBefore);
        assertThat(stockRepository.findByBookId(sufficientBook.getId()).orElseThrow().getQuantity()).isEqualTo(10);
        assertThat(cartRepository.findById(sufficientCartItem.getId())).isPresent();
        assertThat(cartRepository.findById(insufficientCartItem.getId())).isPresent();
    }
}

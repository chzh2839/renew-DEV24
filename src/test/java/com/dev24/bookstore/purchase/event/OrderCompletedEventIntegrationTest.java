package com.dev24.bookstore.purchase.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.LocalDate;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
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
import com.dev24.bookstore.purchase.controller.request.PurchaseRequest;
import com.dev24.bookstore.purchase.domain.Cart;
import com.dev24.bookstore.purchase.domain.Stock;
import com.dev24.bookstore.purchase.domain.enums.PaymentMethod;
import com.dev24.bookstore.purchase.repository.CartRepository;
import com.dev24.bookstore.purchase.repository.StockRepository;
import com.dev24.bookstore.purchase.service.PurchaseCommandService;

// 실제 구매(purchase()) 커밋 -> OrderCompletedEventPublisher가 NATS로 발행 -> 앱 기동 시 이미 구독 중인
// OrderCompletedEventConsumer가 비동기로 소비해 적립금을 지급하는 전체 흐름을 end-to-end로 검증한다.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class OrderCompletedEventIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> nats = new GenericContainer<>("nats:2.10-alpine")
            .withCommand("-js")
            .withExposedPorts(4222);

    @DynamicPropertySource
    static void natsProperties(DynamicPropertyRegistry registry) {
        registry.add("app.nats.enabled", () -> "true");
        registry.add("app.nats.url", () -> "nats://" + nats.getHost() + ":" + nats.getMappedPort(4222));
    }

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

    // 구매 완료 -> NATS 발행 -> 컨슈머 소비 후 적립금(결제금액의 1%)이 비동기로 반영되는지 검증
    @Test
    void purchase_completes_grantsPointAsynchronouslyViaNats() {
        Customer customer = customerRepository.save(new Customer(
                "nats-event-customer", "encoded", "홍길동", "길동이", "nats-event@example.com",
                "010-0000-0000", "서울시 강남구", "소설", false));
        Admin admin = adminRepository.save(new Admin("nats-event-admin", "encoded", "관리자"));
        Book book = bookRepository.save(new Book(
                "9200000000001", "자바의 정석", "남궁성", "도우출판", LocalDate.of(2024, 1, 1),
                10000, "내용", null, "프로그래밍", BookStatus.ACTIVE));
        stockRepository.save(new Stock(book, admin, 10, 12000, 0));
        Cart cartItem = cartRepository.save(new Cart(customer, book, 2, 20000));

        PurchaseRequest request = new PurchaseRequest(List.of(cartItem.getId()), "홍길동", "010-1111-1111",
                "홍길동", "010-1111-1111", "06236", "서울시 강남구", PaymentMethod.CREDIT_CARD);

        purchaseCommandService.purchase("nats-event-customer", request);

        // 소비는 별도 NATS 디스패처 스레드에서 비동기로 일어나므로 즉시 반영을 단언할 수 없어 폴링한다.
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(customerRepository.findById(customer.getId()).orElseThrow().getPoint())
                        .isEqualTo(200));
    }
}

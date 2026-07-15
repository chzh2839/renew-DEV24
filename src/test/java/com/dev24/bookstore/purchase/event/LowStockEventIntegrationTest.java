package com.dev24.bookstore.purchase.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
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
import com.dev24.bookstore.auth.domain.AdminRole;
import com.dev24.bookstore.auth.domain.Customer;
import com.dev24.bookstore.auth.repository.AdminRepository;
import com.dev24.bookstore.auth.repository.CustomerRepository;
import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.book.repository.BookRepository;
import com.dev24.bookstore.common.notification.domain.EmailNotificationStatus;
import com.dev24.bookstore.common.notification.repository.EmailNotificationHistoryRepository;
import com.dev24.bookstore.purchase.controller.request.PurchaseRequest;
import com.dev24.bookstore.purchase.domain.Cart;
import com.dev24.bookstore.purchase.domain.Stock;
import com.dev24.bookstore.purchase.domain.enums.PaymentMethod;
import com.dev24.bookstore.purchase.repository.CartRepository;
import com.dev24.bookstore.purchase.repository.StockRepository;
import com.dev24.bookstore.purchase.service.PurchaseCommandService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// 구매로 재고가 안전재고 이하로 "떨어지는 순간" -> LowStockEventPublisher가 NATS로 발행 -> 앱 기동 시 이미 구독 중인
// LowStockEventConsumer가 비동기로 소비해 재고 관리자(STOCK_ADMIN)에게 실제 이메일(Mailpit)을 발송하고
// email_notification_history에 SUCCESS로 기록하는 전체 흐름을 end-to-end로 검증한다.
// OrderCompletedEventIntegrationTest와 동일한 골격(Postgres+NATS Testcontainers)에 Mailpit 컨테이너만 추가했다.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class LowStockEventIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> nats = new GenericContainer<>("nats:2.10-alpine")
            .withCommand("-js")
            .withExposedPorts(4222);

    @Container
    static GenericContainer<?> mailpit = new GenericContainer<>("axllent/mailpit:latest")
            .withExposedPorts(1025, 8025);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.nats.enabled", () -> "true");
        registry.add("app.nats.url", () -> "nats://" + nats.getHost() + ":" + nats.getMappedPort(4222));
        registry.add("spring.mail.host", mailpit::getHost);
        registry.add("spring.mail.port", () -> mailpit.getMappedPort(1025));
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
    @Autowired
    private EmailNotificationHistoryRepository emailNotificationHistoryRepository;

    // 구매로 재고가 안전재고에 처음 도달 -> NATS 발행 -> 컨슈머 소비 후 재고 관리자에게 실제 메일이 발송되고
    // email_notification_history에 SUCCESS로 남는지, Mailpit에 실제로 도착했는지까지 검증
    @Test
    void purchase_crossesSafetyStock_notifiesStockAdminViaRealMailAsynchronouslyViaNats() throws Exception {
        String stockAdminEmail = "stock-admin@example.com";
        adminRepository.save(new Admin(
                "nats-lowstock-admin", "encoded", "재고관리자", stockAdminEmail, AdminRole.STOCK_ADMIN));
        Customer customer = customerRepository.save(new Customer(
                "nats-lowstock-customer", "encoded", "홍길동", "길동이", "nats-lowstock@example.com",
                "010-0000-0000", "서울시 강남구", "소설", false));
        Admin stockRegistrar = adminRepository.save(new Admin("nats-lowstock-registrar", "encoded", "관리자"));
        Book book = bookRepository.save(new Book(
                "9300000000001", "자바의 정석", "남궁성", "도우출판", LocalDate.of(2024, 1, 1),
                10000, "내용", null, "프로그래밍", BookStatus.ACTIVE));
        // quantity=5, safetyStock=3 -> 2개 구매 시 5->3으로 안전재고에 "처음 도달"해 LowStockEvent가 발행된다
        stockRepository.save(new Stock(book, stockRegistrar, 5, 12000, 3));
        Cart cartItem = cartRepository.save(new Cart(customer, book, 2, 24000));

        PurchaseRequest request = new PurchaseRequest(List.of(cartItem.getId()), "홍길동", "010-1111-1111",
                "홍길동", "010-1111-1111", "06236", "서울시 강남구", PaymentMethod.CREDIT_CARD);

        purchaseCommandService.purchase("nats-lowstock-customer", request);

        // 소비는 별도 NATS 디스패처 스레드에서 비동기로 일어나므로 즉시 반영을 단언할 수 없어 폴링한다.
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertThat(emailNotificationHistoryRepository.findAll())
                .anyMatch(history -> history.getToEmail().equals(stockAdminEmail)
                        && history.getStatus() == EmailNotificationStatus.SUCCESS));

        // 이력 테이블만으로는 "SUCCESS로 기록됐지만 실제로는 안 갔다"는 괴리를 못 잡으므로,
        // Mailpit REST API로 실제 도착 여부까지 직접 확인한다.
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest mailpitRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://" + mailpit.getHost() + ":" + mailpit.getMappedPort(8025) + "/api/v1/messages"))
                .GET()
                .build();
        HttpResponse<String> mailpitResponse = httpClient.send(mailpitRequest, HttpResponse.BodyHandlers.ofString());
        JsonNode body = new ObjectMapper().readTree(mailpitResponse.body());
        assertThat(body.get("messages_count").asInt()).isGreaterThanOrEqualTo(1);
    }
}

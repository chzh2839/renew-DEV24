package com.dev24.bookstore.purchase.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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

    // 재고 1개짜리 Stock을 두 고객이 동시에 구매 시도 -> 하나만 성공해야 하고, 오버셀(음수 재고)은 절대 발생하면 안 된다.
    // 두 스레드가 실제로 같은 순간에 겹쳐 읽으면 나중에 커밋하는 쪽이 Stock.version 충돌로 ObjectOptimisticLockingFailureException을 받고,
    // 겹치지 않고 순차적으로 실행되면 뒤에 실행된 쪽이 이미 소진된 재고를 보고 INSUFFICIENT_STOCK을 받는다
    //  => 스레드 스케줄링에 따라 둘 중 하나가 나오므로 둘 다 허용한다.
    // 주의: ObjectOptimisticLockingFailureException = "오버셀 방지"라는 등식은 재고를 정확히 1개(요청 수량 합계와 동일)로
    // 맞췄을 때만 성립한다. 재고가 넉넉히 남아있는 상태(예: 10개)에서 같은 row를 동시에 갱신하면, 그 자체만으로도
    // (실제로는 둘 다 성공 가능한 수량이어도) version 충돌이 나서 진 쪽이 재시도 없이는 실패한다 - 이건 "재고 부족을
    // 감지해서 막은 것"이 아니라 "같은 row에 대한 순수 쓰기 경합"일 뿐이다. 재고를 1개로 세팅했기 때문에, 진 쪽의
    // 쓰기가 그대로 반영됐다면 quantity가 -1이 됐을 상황이라 이 테스트에 한해서만 "충돌 = 오버셀 방지"가 참이다.
    @Test
    void purchase_concurrentRequestsOnSameStock_onlyOneSucceedsAndNeverOversells() throws Exception {
        Customer customerA = customer("txn-concurrent-a");
        Customer customerB = customer("txn-concurrent-b");
        Admin admin = admin("txn-concurrent-admin");
        Book book = book("9100000000004");
        stockRepository.save(new Stock(book, admin, 1, 12000, 0));
        Cart cartItemA = cartRepository.save(new Cart(customerA, book, 1, 12000));
        Cart cartItemB = cartRepository.save(new Cart(customerB, book, 1, 12000));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        // "출발선" 역할을 하는 2개의 래치로 두 스레드가 최대한 같은 순간에 purchase()를 호출하게 만든다.
        // readyLatch: 두 스레드 모두 "이제 곧 시작한다"고 신호를 보낼 때까지 메인 스레드가 기다리는 용도.
        // startLatch: 두 스레드가 동시에 출발하도록, 메인 스레드가 신호를 줄 때까지 각 스레드를 묶어두는 용도.
        // 이 두 래치가 없으면 스레드 A가 먼저 통째로 실행을 끝내버려 순수 순차 실행이 되어버릴 수 있다(= 충돌 재현 실패).
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        // 서로 다른 고객(A, B)이 같은 책의 재고 1개를 놓고 각자 1개씩 구매를 시도하는 시나리오.
        Callable<PurchaseResponse> taskA = () -> {
            readyLatch.countDown(); // "나 준비 끝났다"고 알림
            startLatch.await();     // 상대방도 준비될 때까지 여기서 대기
            return purchaseCommandService.purchase("txn-concurrent-a", request(List.of(cartItemA.getId())));
        };
        Callable<PurchaseResponse> taskB = () -> {
            readyLatch.countDown();
            startLatch.await();
            return purchaseCommandService.purchase("txn-concurrent-b", request(List.of(cartItemB.getId())));
        };

        // 두 태스크를 스레드풀에 제출만 해두고(아직 시작 안 함, startLatch에 걸려 대기 중),
        Future<PurchaseResponse> futureA = executor.submit(taskA);
        Future<PurchaseResponse> futureB = executor.submit(taskB);
        readyLatch.await(5, TimeUnit.SECONDS); // 둘 다 대기 지점(startLatch.await() 직전)에 도달할 때까지 기다렸다가
        startLatch.countDown();                // 동시에 출발시킨다 -> 두 트랜잭션의 Stock 조회가 겹칠 확률을 최대화

        // future.get()이 예외를 던지면 성공, ExecutionException(실제 원인은 getCause())이면 실패로 분류해서 모은다.
        List<PurchaseResponse> successes = new ArrayList<>();
        List<Throwable> failures = new ArrayList<>();
        for (Future<PurchaseResponse> future : List.of(futureA, futureB)) {
            try {
                successes.add(future.get(10, TimeUnit.SECONDS));
            } catch (ExecutionException e) {
                failures.add(e.getCause());
            }
        }
        executor.shutdown();

        // 핵심 검증: 재고가 1개뿐이므로 둘 중 정확히 하나만 성공해야 하고(=오버셀 없음),
        assertThat(successes).hasSize(1);
        assertThat(failures).hasSize(1);
        // 실패한 나머지 하나의 원인은 두 가지 중 하나일 수 있다(둘 다 "오버셀 방지"라는 결과는 동일, 원인만 다름):
        //  1) ObjectOptimisticLockingFailureException: 두 트랜잭션의 조회가 실제로 겹쳐서
        //     Stock.version 충돌이 난 경우 (이 테스트가 원래 검증하고 싶은 낙관적 락 케이스)
        //  2) BusinessException(INSUFFICIENT_STOCK): 스케줄링상 겹치지 못하고 순차 실행되어,
        //     뒤에 실행된 쪽이 이미 0으로 소진된 재고를 보고 정상적으로 거부된 경우
        // 두 트랜잭션이 실제로 겹치는지는 OS 스레드 스케줄링에 달려있어 100% 보장할 수 없으므로,
        // "어떤 이유로 실패했는지"보다 "오버셀 없이 정확히 하나만 성공했는지"를 검증의 핵심으로 삼아 둘 다 허용한다.
        assertThat(failures.get(0)).matches(t -> t instanceof ObjectOptimisticLockingFailureException
                || (t instanceof BusinessException be && be.getErrorCode() == ErrorCode.INSUFFICIENT_STOCK));

        // 마지막 안전장치: 어떤 경로로 실패했든 최종 재고가 음수로 내려가진 않았는지(진짜 오버셀 여부) 직접 확인
        assertThat(stockRepository.findByBookId(book.getId()).orElseThrow().getQuantity()).isEqualTo(0);
    }
}

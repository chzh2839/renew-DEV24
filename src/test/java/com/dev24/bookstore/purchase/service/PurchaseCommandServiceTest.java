package com.dev24.bookstore.purchase.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import com.dev24.bookstore.auth.domain.Admin;
import com.dev24.bookstore.auth.domain.Customer;
import com.dev24.bookstore.auth.repository.CustomerRepository;
import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.common.exception.BusinessException;
import com.dev24.bookstore.common.exception.ErrorCode;
import com.dev24.bookstore.purchase.controller.request.PurchaseRequest;
import com.dev24.bookstore.purchase.controller.response.PurchaseResponse;
import com.dev24.bookstore.purchase.domain.Cart;
import com.dev24.bookstore.purchase.domain.Stock;
import com.dev24.bookstore.purchase.domain.enums.PaymentMethod;
import com.dev24.bookstore.purchase.event.LowStockEvent;
import com.dev24.bookstore.purchase.repository.CartRepository;
import com.dev24.bookstore.purchase.repository.PurchaseItemRepository;
import com.dev24.bookstore.purchase.repository.PurchaseRepository;
import com.dev24.bookstore.purchase.repository.StockRepository;

@ExtendWith(MockitoExtension.class)
class PurchaseCommandServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private PurchaseItemRepository purchaseItemRepository;
    @Mock
    private StockRepository stockRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private PurchaseCommandService purchaseCommandService;

    @BeforeEach
    void setUp() {
        purchaseCommandService = new PurchaseCommandService(customerRepository, cartRepository, purchaseRepository,
                purchaseItemRepository, stockRepository, applicationEventPublisher);
    }

    // 실제로는 JPA가 채워주는 id를, Mockito 단위테스트에선 리포지토리가 엔티티를 반환만 하고 영속화하지 않으므로
    // 소유권 비교(getId().equals(...))가 의미를 가지도록 리플렉션으로 직접 채워준다.
    private Customer customer(long id) {
        Customer customer = new Customer("customer1", "encoded", "홍길동", "길동이",
                "customer1@example.com", "010-0000-0000", "서울시", "소설", false);
        ReflectionTestUtils.setField(customer, "id", id);
        return customer;
    }

    private Book book(long id, String isbn) {
        Book book = new Book(isbn, "자바의 정석", "남궁성", "도우출판", LocalDate.of(2024, 1, 1),
                10000, "내용", null, "프로그래밍", BookStatus.ACTIVE);
        ReflectionTestUtils.setField(book, "id", id);
        return book;
    }

    private PurchaseRequest request(List<Long> cartItemIds) {
        return new PurchaseRequest(cartItemIds, "홍길동", "010-1111-1111", "홍길동", "010-1111-1111",
                "06236", "서울시 강남구", PaymentMethod.CREDIT_CARD);
    }

    // 재고가 충분하면 주문(Purchase/PurchaseItem)이 생성되고, 재고가 차감되고, 장바구니 항목이 삭제되는지 검증
    @Test
    void purchase_sufficientStock_createsPurchaseAndDecrementsStockAndClearsCart() {
        Customer customer = customer(1L);
        Book book = book(1L, "9000000000201");
        Cart cartItem = new Cart(customer, book, 2, 20000);
        Stock stock = new Stock(book, new Admin("admin1", "encoded", "관리자"), 10, 12000, 2);
        given(customerRepository.findByLoginId("customer1")).willReturn(Optional.of(customer));
        given(cartRepository.findAllById(List.of(1L))).willReturn(List.of(cartItem));
        given(stockRepository.findByBookId(book.getId())).willReturn(Optional.of(stock));
        given(purchaseRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        PurchaseResponse response = purchaseCommandService.purchase("customer1", request(List.of(1L)));

        assertThat(response.totalPrice()).isEqualTo(20000);
        assertThat(response.paymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(stock.getQuantity()).isEqualTo(8);
        verify(purchaseItemRepository).save(any());
        verify(cartRepository).deleteAll(List.of(cartItem));
    }

    // 재고가 부족하면 INSUFFICIENT_STOCK을 던지고, 그 이후 단계인 장바구니 삭제는 호출되지 않는지 검증
    @Test
    void purchase_insufficientStock_throwsAndDoesNotClearCart() {
        Customer customer = customer(2L);
        Book book = book(2L, "9000000000202");
        Cart cartItem = new Cart(customer, book, 5, 50000);
        Stock stock = new Stock(book, new Admin("admin1", "encoded", "관리자"), 1, 12000, 0);
        given(customerRepository.findByLoginId("customer1")).willReturn(Optional.of(customer));
        given(cartRepository.findAllById(List.of(1L))).willReturn(List.of(cartItem));
        given(stockRepository.findByBookId(book.getId())).willReturn(Optional.of(stock));
        given(purchaseRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> purchaseCommandService.purchase("customer1", request(List.of(1L))))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INSUFFICIENT_STOCK);

        verify(cartRepository, never()).deleteAll(any());
    }

    // 재고는 요청 수량보다 많아도 (재고 - 안전재고)보다 요청 수량이 크면 INSUFFICIENT_STOCK을 던지는지 검증
    // (quantity만 보던 예전 로직이라면 10 >= 3이라 통과했을 케이스)
    @Test
    void purchase_belowSafetyStockThreshold_throwsInsufficientStock() {
        Customer customer = customer(6L);
        Book book = book(6L, "9000000000206");
        Cart cartItem = new Cart(customer, book, 3, 30000);
        Stock stock = new Stock(book, new Admin("admin1", "encoded", "관리자"), 10, 12000, 8);
        given(customerRepository.findByLoginId("customer1")).willReturn(Optional.of(customer));
        given(cartRepository.findAllById(List.of(1L))).willReturn(List.of(cartItem));
        given(stockRepository.findByBookId(book.getId())).willReturn(Optional.of(stock));

        assertThatThrownBy(() -> purchaseCommandService.purchase("customer1", request(List.of(1L))))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INSUFFICIENT_STOCK);

        verify(cartRepository, never()).deleteAll(any());
    }

    // 구매로 재고가 안전재고 이하로 "처음 떨어지는" 순간엔 LowStockEvent가 발행되는지 검증
    @Test
    void purchase_crossesSafetyStockThreshold_publishesLowStockEvent() {
        Customer customer = customer(7L);
        Book book = book(7L, "9000000000207");
        Admin admin = new Admin("admin1", "encoded", "관리자");
        ReflectionTestUtils.setField(admin, "id", 1L);
        Cart cartItem = new Cart(customer, book, 2, 20000);
        Stock stock = new Stock(book, admin, 5, 12000, 3); // quantity=5, safetyStock=3 -> 2개 구매 시 3으로 하락(임계값 도달)
        given(customerRepository.findByLoginId("customer1")).willReturn(Optional.of(customer));
        given(cartRepository.findAllById(List.of(1L))).willReturn(List.of(cartItem));
        given(stockRepository.findByBookId(book.getId())).willReturn(Optional.of(stock));
        given(purchaseRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        purchaseCommandService.purchase("customer1", request(List.of(1L)));

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher, atLeastOnce()).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getAllValues())
                .filteredOn(event -> event instanceof LowStockEvent)
                .singleElement()
                .satisfies(event -> {
                    LowStockEvent lowStockEvent = (LowStockEvent) event;
                    assertThat(lowStockEvent.bookId()).isEqualTo(7L);
                    assertThat(lowStockEvent.adminId()).isEqualTo(1L);
                    assertThat(lowStockEvent.remainingQuantity()).isEqualTo(3);
                    assertThat(lowStockEvent.safetyStock()).isEqualTo(3);
                });
    }

    // 구매 후에도 안전재고보다 여유가 있으면(임계값을 넘지 않으면) LowStockEvent가 발행되지 않는지 검증
    @Test
    void purchase_staysAboveSafetyStockThreshold_doesNotPublishLowStockEvent() {
        Customer customer = customer(8L);
        Book book = book(8L, "9000000000208");
        Admin admin = new Admin("admin1", "encoded", "관리자");
        ReflectionTestUtils.setField(admin, "id", 1L);
        Cart cartItem = new Cart(customer, book, 2, 20000);
        Stock stock = new Stock(book, admin, 10, 12000, 2); // quantity=10, safetyStock=2 -> 2개 구매해도 8로, 임계값 안 넘음
        given(customerRepository.findByLoginId("customer1")).willReturn(Optional.of(customer));
        given(cartRepository.findAllById(List.of(1L))).willReturn(List.of(cartItem));
        given(stockRepository.findByBookId(book.getId())).willReturn(Optional.of(stock));
        given(purchaseRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        purchaseCommandService.purchase("customer1", request(List.of(1L)));

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher, atLeastOnce()).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getAllValues()).noneMatch(event -> event instanceof LowStockEvent);
    }

    // 다른 고객 소유의 장바구니 항목이면 ENTITY_NOT_FOUND를 던지는지 검증
    // (403이 아닌 404로 응답해 "존재하지만 내 것이 아니다"라는 사실 자체를 감춘다)
    @Test
    void purchase_cartItemNotOwnedByCustomer_throwsEntityNotFound() {
        Customer customer = customer(3L);
        Customer otherCustomer = new Customer("other", "encoded", "타인", "타인닉네임",
                "other@example.com", "010-2222-2222", "서울시", "소설", false);
        ReflectionTestUtils.setField(otherCustomer, "id", 4L);
        Book book = book(3L, "9000000000203");
        Cart othersCartItem = new Cart(otherCustomer, book, 1, 10000);
        given(customerRepository.findByLoginId("customer1")).willReturn(Optional.of(customer));
        given(cartRepository.findAllById(List.of(1L))).willReturn(List.of(othersCartItem));

        assertThatThrownBy(() -> purchaseCommandService.purchase("customer1", request(List.of(1L))))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    // 요청한 cartItemIds 중 존재하지 않는 id가 섞여 있으면(조회 결과 개수가 다르면) ENTITY_NOT_FOUND를 던지는지 검증
    @Test
    void purchase_missingCartItemId_throwsEntityNotFound() {
        given(customerRepository.findByLoginId("customer1")).willReturn(Optional.of(customer(5L)));
        given(cartRepository.findAllById(List.of(1L, 2L))).willReturn(List.of());

        assertThatThrownBy(() -> purchaseCommandService.purchase("customer1", request(List.of(1L, 2L))))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    // 인증은 됐지만 대응하는 Customer 레코드가 없는 loginId면 ENTITY_NOT_FOUND를 던지는지 검증
    @Test
    void purchase_unknownLoginId_throwsEntityNotFound() {
        given(customerRepository.findByLoginId("unknown")).willReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseCommandService.purchase("unknown", request(List.of(1L))))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }
}

package com.dev24.bookstore.purchase.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.dev24.bookstore.auth.domain.Admin;
import com.dev24.bookstore.auth.domain.Customer;
import com.dev24.bookstore.auth.repository.CustomerRepository;
import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.book.repository.BookRepository;
import com.dev24.bookstore.common.exception.BusinessException;
import com.dev24.bookstore.common.exception.ErrorCode;
import com.dev24.bookstore.purchase.controller.request.CartAddRequest;
import com.dev24.bookstore.purchase.controller.response.CartResponse;
import com.dev24.bookstore.purchase.domain.Cart;
import com.dev24.bookstore.purchase.domain.Stock;
import com.dev24.bookstore.purchase.repository.CartRepository;
import com.dev24.bookstore.purchase.repository.StockRepository;

@ExtendWith(MockitoExtension.class)
class CartCommandServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private StockRepository stockRepository;
    @Mock
    private CartRepository cartRepository;

    private CartCommandService cartCommandService;

    @BeforeEach
    void setUp() {
        cartCommandService = new CartCommandService(customerRepository, bookRepository, stockRepository, cartRepository);
    }

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

    // 처음 담는 책이면 새 Cart가 생성되는지 검증
    @Test
    void addToCart_newBook_createsCart() {
        Customer customer = customer(1L);
        Book book = book(1L, "9000000000301");
        Stock stock = new Stock(book, new Admin("admin1", "encoded", "관리자"), 10, 12000, 2);
        given(customerRepository.findByLoginId("customer1")).willReturn(Optional.of(customer));
        given(bookRepository.findById(1L)).willReturn(Optional.of(book));
        given(stockRepository.findByBookId(1L)).willReturn(Optional.of(stock));
        given(cartRepository.findByCustomerIdAndBookId(1L, 1L)).willReturn(Optional.empty());
        given(cartRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        CartResponse response = cartCommandService.addToCart("customer1", new CartAddRequest(1L, 2));

        assertThat(response.bookId()).isEqualTo(1L);
        assertThat(response.quantity()).isEqualTo(2);
        assertThat(response.priceSnapshot()).isEqualTo(24000);
    }

    // 이미 담긴 책을 또 담으면 새 행을 만들지 않고 기존 행의 수량/금액에 합쳐지는지(merge) 검증
    @Test
    void addToCart_existingBook_mergesIntoExistingCartItem() {
        Customer customer = customer(2L);
        Book book = book(2L, "9000000000302");
        Stock stock = new Stock(book, new Admin("admin1", "encoded", "관리자"), 10, 12000, 0);
        Cart existingCartItem = new Cart(customer, book, 1, 12000);
        given(customerRepository.findByLoginId("customer1")).willReturn(Optional.of(customer));
        given(bookRepository.findById(2L)).willReturn(Optional.of(book));
        given(stockRepository.findByBookId(2L)).willReturn(Optional.of(stock));
        given(cartRepository.findByCustomerIdAndBookId(2L, 2L)).willReturn(Optional.of(existingCartItem));

        CartResponse response = cartCommandService.addToCart("customer1", new CartAddRequest(2L, 2));

        assertThat(response.quantity()).isEqualTo(3);
        assertThat(response.priceSnapshot()).isEqualTo(36000);
        verify(cartRepository, never()).save(any());
    }

    // 존재하지 않는 도서면 ENTITY_NOT_FOUND를 던지는지 검증
    @Test
    void addToCart_unknownBook_throwsEntityNotFound() {
        given(customerRepository.findByLoginId("customer1")).willReturn(Optional.of(customer(3L)));
        given(bookRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> cartCommandService.addToCart("customer1", new CartAddRequest(99L, 1)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    // 도서는 있지만 재고 등록 자체가 없으면 ENTITY_NOT_FOUND를 던지는지 검증
    @Test
    void addToCart_bookWithoutStock_throwsEntityNotFound() {
        Book book = book(4L, "9000000000304");
        given(customerRepository.findByLoginId("customer1")).willReturn(Optional.of(customer(4L)));
        given(bookRepository.findById(4L)).willReturn(Optional.of(book));
        given(stockRepository.findByBookId(4L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> cartCommandService.addToCart("customer1", new CartAddRequest(4L, 1)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    // 요청 수량이 구매 가능 수량(quantity - safetyStock)을 초과하면 INSUFFICIENT_STOCK을 던지는지 검증
    @Test
    void addToCart_exceedsAvailableStock_throwsInsufficientStock() {
        Customer customer = customer(5L);
        Book book = book(5L, "9000000000305");
        Stock stock = new Stock(book, new Admin("admin1", "encoded", "관리자"), 5, 12000, 3);
        given(customerRepository.findByLoginId("customer1")).willReturn(Optional.of(customer));
        given(bookRepository.findById(5L)).willReturn(Optional.of(book));
        given(stockRepository.findByBookId(5L)).willReturn(Optional.of(stock));
        given(cartRepository.findByCustomerIdAndBookId(5L, 5L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> cartCommandService.addToCart("customer1", new CartAddRequest(5L, 3)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INSUFFICIENT_STOCK);
        verify(cartRepository, never()).save(any());
    }

    // 기존 담긴 수량 + 새로 담는 수량의 합이 구매 가능 수량을 초과해도 INSUFFICIENT_STOCK을 던지는지 검증
    @Test
    void addToCart_mergedQuantityExceedsAvailableStock_throwsInsufficientStock() {
        Customer customer = customer(6L);
        Book book = book(6L, "9000000000306");
        Stock stock = new Stock(book, new Admin("admin1", "encoded", "관리자"), 5, 12000, 0);
        Cart existingCartItem = new Cart(customer, book, 4, 48000);
        given(customerRepository.findByLoginId("customer1")).willReturn(Optional.of(customer));
        given(bookRepository.findById(6L)).willReturn(Optional.of(book));
        given(stockRepository.findByBookId(6L)).willReturn(Optional.of(stock));
        given(cartRepository.findByCustomerIdAndBookId(6L, 6L)).willReturn(Optional.of(existingCartItem));

        assertThatThrownBy(() -> cartCommandService.addToCart("customer1", new CartAddRequest(6L, 2)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INSUFFICIENT_STOCK);
        assertThat(existingCartItem.getQuantity()).isEqualTo(4);
    }

    // 인증은 됐지만 대응하는 Customer 레코드가 없는 loginId면 ENTITY_NOT_FOUND를 던지는지 검증
    @Test
    void addToCart_unknownLoginId_throwsEntityNotFound() {
        given(customerRepository.findByLoginId("unknown")).willReturn(Optional.empty());

        assertThatThrownBy(() -> cartCommandService.addToCart("unknown", new CartAddRequest(1L, 1)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }
}

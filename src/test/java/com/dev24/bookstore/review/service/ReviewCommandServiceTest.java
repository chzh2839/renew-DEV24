package com.dev24.bookstore.review.service;

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

import com.dev24.bookstore.auth.domain.Customer;
import com.dev24.bookstore.auth.repository.CustomerRepository;
import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.common.exception.BusinessException;
import com.dev24.bookstore.common.exception.ErrorCode;
import com.dev24.bookstore.common.sanitizer.HtmlSanitizer;
import com.dev24.bookstore.purchase.domain.Purchase;
import com.dev24.bookstore.purchase.domain.PurchaseItem;
import com.dev24.bookstore.purchase.domain.enums.PaymentMethod;
import com.dev24.bookstore.purchase.repository.PurchaseItemRepository;
import com.dev24.bookstore.review.controller.request.ReviewCreateRequest;
import com.dev24.bookstore.review.controller.request.ReviewUpdateRequest;
import com.dev24.bookstore.review.controller.response.ReviewResponse;
import com.dev24.bookstore.review.domain.Review;
import com.dev24.bookstore.review.domain.ReviewType;
import com.dev24.bookstore.review.repository.ReviewRepository;

@ExtendWith(MockitoExtension.class)
class ReviewCommandServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private PurchaseItemRepository purchaseItemRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private HtmlSanitizer htmlSanitizer;

    private ReviewCommandService reviewCommandService;

    @BeforeEach
    void setUp() {
        reviewCommandService = new ReviewCommandService(
                customerRepository, purchaseItemRepository, reviewRepository, htmlSanitizer);
    }

    private Customer customer(long id, String loginId) {
        Customer customer = new Customer(loginId, "encoded", "홍길동", "길동이",
                loginId + "@example.com", "010-0000-0000", "서울시", "소설", false);
        ReflectionTestUtils.setField(customer, "id", id);
        return customer;
    }

    private Book book(long id) {
        Book book = new Book("9000000000501", "자바의 정석", "남궁성", "도우출판", LocalDate.of(2024, 1, 1),
                10000, "내용", null, "프로그래밍", BookStatus.ACTIVE);
        ReflectionTestUtils.setField(book, "id", id);
        return book;
    }

    private PurchaseItem purchaseItem(long id, Customer owner, Book book) {
        Purchase purchase = new Purchase(owner, "홍길동", "010-1111-1111", "홍길동", "010-1111-1111",
                "06236", "서울시 강남구", PaymentMethod.CREDIT_CARD, 20000);
        PurchaseItem purchaseItem = new PurchaseItem(purchase, book, 1, 10000);
        ReflectionTestUtils.setField(purchaseItem, "id", id);
        return purchaseItem;
    }

    // 실제로 구매한 본인이면 리뷰가 생성되고, content가 HtmlSanitizer를 거친 결과로 저장되는지 검증
    @Test
    void createReview_ownedPurchaseItem_createsReviewWithSanitizedContent() {
        Customer customer = customer(1L, "customer1");
        Book book = book(1L);
        PurchaseItem purchaseItem = purchaseItem(1L, customer, book);
        given(customerRepository.findByLoginId("customer1")).willReturn(Optional.of(customer));
        given(purchaseItemRepository.findById(1L)).willReturn(Optional.of(purchaseItem));
        given(htmlSanitizer.sanitize("<script>alert(1)</script>좋아요")).willReturn("좋아요");
        given(reviewRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        ReviewResponse response = reviewCommandService.createReview(
                "customer1", new ReviewCreateRequest(1L, 5, "<script>alert(1)</script>좋아요", ReviewType.TEXT, null));

        assertThat(response.score()).isEqualTo(5);
        assertThat(response.content()).isEqualTo("좋아요");
        assertThat(response.bookId()).isEqualTo(1L);
    }

    // 다른 사람이 구매한 purchaseItem으로 리뷰를 남기려 하면 ENTITY_NOT_FOUND(404)를 던지는지 검증
    // - 403이 아닌 404인 이유: PurchaseCommandService의 장바구니 소유권 검증과 동일하게 존재 여부 자체를 감춘다
    @Test
    void createReview_purchaseItemNotOwnedByCustomer_throwsEntityNotFound() {
        Customer customer = customer(2L, "customer1");
        Customer otherCustomer = customer(3L, "other");
        Book book = book(2L);
        PurchaseItem purchaseItem = purchaseItem(2L, otherCustomer, book);
        given(customerRepository.findByLoginId("customer1")).willReturn(Optional.of(customer));
        given(purchaseItemRepository.findById(2L)).willReturn(Optional.of(purchaseItem));

        assertThatThrownBy(() -> reviewCommandService.createReview(
                "customer1", new ReviewCreateRequest(2L, 5, "내용", ReviewType.TEXT, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        verify(reviewRepository, never()).save(any());
    }

    // 존재하지 않는 purchaseItemId면 ENTITY_NOT_FOUND를 던지는지 검증
    @Test
    void createReview_unknownPurchaseItem_throwsEntityNotFound() {
        given(customerRepository.findByLoginId("customer1")).willReturn(Optional.of(customer(4L, "customer1")));
        given(purchaseItemRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewCommandService.createReview(
                "customer1", new ReviewCreateRequest(99L, 5, "내용", ReviewType.TEXT, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    // 본인 리뷰는 수정 가능하고, sanitize된 content가 반영되는지 검증
    @Test
    void updateReview_owner_updatesReviewWithSanitizedContent() {
        Customer customer = customer(5L, "customer1");
        Book book = book(5L);
        PurchaseItem purchaseItem = purchaseItem(5L, customer, book);
        Review review = new Review(customer, book, purchaseItem, 3, "그냥 그래요", ReviewType.TEXT, null);
        given(customerRepository.findByLoginId("customer1")).willReturn(Optional.of(customer));
        given(reviewRepository.findById(10L)).willReturn(Optional.of(review));
        given(htmlSanitizer.sanitize("역시 좋아요")).willReturn("역시 좋아요");

        ReviewResponse response = reviewCommandService.updateReview(
                "customer1", 10L, new ReviewUpdateRequest(5, "역시 좋아요", ReviewType.TEXT, null));

        assertThat(response.score()).isEqualTo(5);
        assertThat(response.content()).isEqualTo("역시 좋아요");
    }

    // 타인 리뷰를 수정하려 하면 ENTITY_NOT_FOUND를 던지고 원본이 그대로인지 검증
    @Test
    void updateReview_notOwner_throwsEntityNotFoundAndDoesNotModify() {
        Customer owner = customer(6L, "owner");
        Customer stranger = customer(7L, "stranger");
        Book book = book(6L);
        PurchaseItem purchaseItem = purchaseItem(6L, owner, book);
        Review review = new Review(owner, book, purchaseItem, 3, "내용", ReviewType.TEXT, null);
        given(customerRepository.findByLoginId("stranger")).willReturn(Optional.of(stranger));
        given(reviewRepository.findById(11L)).willReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewCommandService.updateReview(
                "stranger", 11L, new ReviewUpdateRequest(1, "악의적 수정", ReviewType.TEXT, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        assertThat(review.getScore()).isEqualTo(3);
        assertThat(review.getContent()).isEqualTo("내용");
    }

    // 본인 리뷰는 삭제 가능한지 검증
    @Test
    void deleteReview_owner_deletesReview() {
        Customer customer = customer(8L, "customer1");
        Book book = book(8L);
        PurchaseItem purchaseItem = purchaseItem(8L, customer, book);
        Review review = new Review(customer, book, purchaseItem, 3, "내용", ReviewType.TEXT, null);
        given(customerRepository.findByLoginId("customer1")).willReturn(Optional.of(customer));
        given(reviewRepository.findById(12L)).willReturn(Optional.of(review));

        reviewCommandService.deleteReview("customer1", 12L);

        verify(reviewRepository).delete(review);
    }

    // 타인 리뷰를 삭제하려 하면 ENTITY_NOT_FOUND를 던지고 삭제되지 않는지 검증
    @Test
    void deleteReview_notOwner_throwsEntityNotFoundAndDoesNotDelete() {
        Customer owner = customer(9L, "owner");
        Customer stranger = customer(10L, "stranger");
        Book book = book(9L);
        PurchaseItem purchaseItem = purchaseItem(9L, owner, book);
        Review review = new Review(owner, book, purchaseItem, 3, "내용", ReviewType.TEXT, null);
        given(customerRepository.findByLoginId("stranger")).willReturn(Optional.of(stranger));
        given(reviewRepository.findById(13L)).willReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewCommandService.deleteReview("stranger", 13L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        verify(reviewRepository, never()).delete(any());
    }
}

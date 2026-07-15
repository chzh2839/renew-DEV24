package com.dev24.bookstore.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.dev24.bookstore.auth.domain.Customer;
import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.purchase.domain.Purchase;
import com.dev24.bookstore.purchase.domain.PurchaseItem;
import com.dev24.bookstore.purchase.domain.enums.PaymentMethod;
import com.dev24.bookstore.review.controller.response.ReviewResponse;
import com.dev24.bookstore.review.domain.Review;
import com.dev24.bookstore.review.domain.ReviewType;
import com.dev24.bookstore.review.repository.ReviewRepository;

@ExtendWith(MockitoExtension.class)
class ReviewQueryServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    private ReviewQueryService reviewQueryService;

    @BeforeEach
    void setUp() {
        reviewQueryService = new ReviewQueryService(reviewRepository);
    }

    // reviewRepository가 반환한 Page<Review>가 Page<ReviewResponse>로 올바르게 매핑되는지(내용/총 개수) 검증
    @Test
    void getReviews_mapsPageOfReviewToPageOfReviewResponse() {
        Customer customer = new Customer("customer1", "encoded", "홍길동", "길동이",
                "customer1@example.com", "010-0000-0000", "서울시", "소설", false);
        ReflectionTestUtils.setField(customer, "id", 1L);
        Book book = new Book("9000000000601", "자바의 정석", "남궁성", "도우출판", LocalDate.of(2024, 1, 1),
                10000, "내용", null, "프로그래밍", BookStatus.ACTIVE);
        ReflectionTestUtils.setField(book, "id", 1L);
        Purchase purchase = new Purchase(customer, "홍길동", "010-1111-1111", "홍길동", "010-1111-1111",
                "06236", "서울시 강남구", PaymentMethod.CREDIT_CARD, 20000);
        PurchaseItem purchaseItem = new PurchaseItem(purchase, book, 1, 10000);
        Review review = new Review(customer, book, purchaseItem, 5, "정말 좋아요", ReviewType.TEXT, null);
        Pageable pageable = PageRequest.of(0, 20);
        given(reviewRepository.findAllByBookId(1L, pageable))
                .willReturn(new PageImpl<>(List.of(review), pageable, 1));

        var result = reviewQueryService.getReviews(1L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        ReviewResponse response = result.getContent().get(0);
        assertThat(response.bookId()).isEqualTo(1L);
        assertThat(response.customerId()).isEqualTo(1L);
        assertThat(response.score()).isEqualTo(5);
        assertThat(response.content()).isEqualTo("정말 좋아요");
    }
}

package com.dev24.bookstore.review.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.dev24.bookstore.auth.domain.Customer;
import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.common.storage.ImageMagicBytesValidator;
import com.dev24.bookstore.purchase.domain.Purchase;
import com.dev24.bookstore.purchase.domain.PurchaseItem;
import com.dev24.bookstore.purchase.domain.enums.PaymentMethod;
import com.dev24.bookstore.review.domain.Review;
import com.dev24.bookstore.review.domain.ReviewType;
import com.dev24.bookstore.review.repository.ReviewRepository;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@ExtendWith(MockitoExtension.class)
class ReviewImageValidationListenerTest {

    @Mock
    private S3Client s3Client;
    @Mock
    private ImageMagicBytesValidator imageMagicBytesValidator;
    @Mock
    private ReviewRepository reviewRepository;

    private ReviewImageValidationListener listener;

    @BeforeEach
    void setUp() {
        listener = new ReviewImageValidationListener(s3Client, imageMagicBytesValidator, reviewRepository);
        ReflectionTestUtils.setField(listener, "bucket", "review-images");
    }

    private Review review(long id) {
        Customer customer = new Customer("customer1", "encoded", "홍길동", "길동이",
                "customer1@example.com", "010-0000-0000", "서울시", "소설", false);
        Book book = new Book("9000000000701", "자바의 정석", "남궁성", "도우출판", LocalDate.of(2024, 1, 1),
                10000, "내용", null, "프로그래밍", BookStatus.ACTIVE);
        Purchase purchase = new Purchase(customer, "홍길동", "010-1111-1111", "홍길동", "010-1111-1111",
                "06236", "서울시 강남구", PaymentMethod.CREDIT_CARD, 20000);
        PurchaseItem purchaseItem = new PurchaseItem(purchase, book, 1, 10000);
        Review review = new Review(customer, book, purchaseItem, 5, "좋아요", ReviewType.IMAGE, "reviews/photo.jpg");
        ReflectionTestUtils.setField(review, "id", id);
        return review;
    }

    private ResponseInputStream<GetObjectResponse> responseWith(byte[] bytes) {
        return new ResponseInputStream<>(GetObjectResponse.builder().build(),
                AbortableInputStream.create(new ByteArrayInputStream(bytes)));
    }

    // 진짜 이미지면 오브젝트 삭제도, 리뷰 이미지 참조 정리도 안 일어나는지 검증
    @Test
    void handle_validImage_doesNotDeleteObjectOrClearImage() {
        given(s3Client.getObject(any(GetObjectRequest.class))).willReturn(responseWith(new byte[]{1, 2, 3}));
        given(imageMagicBytesValidator.isValidImage(any(), eq("reviews/photo.jpg"))).willReturn(true);

        listener.handle(new ReviewImageUploadedEvent(1L, "reviews/photo.jpg"));

        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
        verify(reviewRepository, never()).findById(any());
    }

    // 진짜 이미지가 아니면 스토리지에서 오브젝트를 삭제하고, 리뷰의 이미지 참조를 정리하는지 검증
    @Test
    void handle_invalidImage_deletesObjectAndClearsReviewImage() {
        Review review = review(1L);
        given(s3Client.getObject(any(GetObjectRequest.class))).willReturn(responseWith(new byte[]{1, 2, 3}));
        given(imageMagicBytesValidator.isValidImage(any(), eq("reviews/photo.jpg"))).willReturn(false);
        given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

        listener.handle(new ReviewImageUploadedEvent(1L, "reviews/photo.jpg"));

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
        assertThat(review.getImageUrl()).isNull();
        assertThat(review.getType()).isEqualTo(ReviewType.TEXT);
    }

    // 오브젝트를 읽는 것 자체가 실패하면(예: 아직 업로드 안 됐거나 이미 삭제됨) 리뷰의 이미지 참조를 정리하는지 검증
    @Test
    void handle_objectReadFails_clearsReviewImage() {
        Review review = review(1L);
        willThrow(NoSuchKeyException.builder().message("no such key").build())
                .given(s3Client).getObject(any(GetObjectRequest.class));
        given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

        listener.handle(new ReviewImageUploadedEvent(1L, "reviews/photo.jpg"));

        assertThat(review.getImageUrl()).isNull();
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }
}

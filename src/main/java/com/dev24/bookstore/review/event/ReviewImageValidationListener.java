package com.dev24.bookstore.review.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.dev24.bookstore.common.storage.ImageMagicBytesValidator;
import com.dev24.bookstore.review.domain.Review;
import com.dev24.bookstore.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

// ReviewCommandService가 발행한 ReviewImageUploadedEvent를 받아, 커밋 후(리뷰 row가 실제로 존재함을 보장) +
// 별도 스레드에서(요청을 안 막음) 업로드된 오브젝트가 진짜 이미지인지 검증한다.
// ReviewCommandService 안에 두지 않고 별도 빈으로 둔 이유: @Async는 자기 자신을 직접 호출(self-invocation)하면
// 프록시를 안 타 조용히 무시된다(docs/NATS.md에 정리된 @Transactional self-invocation 문제와 동일한 함정).
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewImageValidationListener {

    private final S3Client s3Client;
    private final ImageMagicBytesValidator imageMagicBytesValidator;
    private final ReviewRepository reviewRepository;

    @Value("${app.storage.bucket}")
    private String bucket;

    // AFTER_COMMIT 시점엔 원본 트랜잭션이 이미 끝나있어 REQUIRED로는 참여할 트랜잭션이 없다 -
    // clearInvalidImage()의 더티체킹이 반영되려면 REQUIRES_NEW로 새 트랜잭션을 열어야 한다
    // (Spring이 @TransactionalEventListener(AFTER_COMMIT) + 일반 @Transactional 조합 자체를 기동 시점에 막는다).
    @Async("reviewImageValidationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(ReviewImageUploadedEvent event) {
        byte[] content;
        try {
            content = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucket).key(event.objectKey()).build()).readAllBytes();
        } catch (Exception e) {
            log.warn("검증할 이미지 객체를 읽지 못함 - reviewId={}, objectKey={}", event.reviewId(), event.objectKey(), e);
            clearInvalidImage(event);
            return;
        }

        if (!imageMagicBytesValidator.isValidImage(content, event.objectKey())) {
            log.warn("업로드된 파일이 실제 이미지가 아님 - reviewId={}, objectKey={}", event.reviewId(), event.objectKey());
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(event.objectKey()).build());
            clearInvalidImage(event);
        }
    }

    private void clearInvalidImage(ReviewImageUploadedEvent event) {
        reviewRepository.findById(event.reviewId()).ifPresent(Review::clearImage);
    }
}

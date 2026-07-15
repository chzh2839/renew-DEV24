package com.dev24.bookstore.review.schedule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.dev24.bookstore.review.repository.ReviewRepository;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

@ExtendWith(MockitoExtension.class)
class OrphanedReviewImageCleanupJobTest {

    @Mock
    private S3Client s3Client;
    @Mock
    private ReviewRepository reviewRepository;

    private OrphanedReviewImageCleanupJob job;

    @BeforeEach
    void setUp() {
        job = new OrphanedReviewImageCleanupJob(s3Client, reviewRepository);
        ReflectionTestUtils.setField(job, "bucket", "review-images");
        ReflectionTestUtils.setField(job, "minAgeMinutes", 60L);
    }

    private S3Object object(String key, Instant lastModified) {
        return S3Object.builder().key(key).lastModified(lastModified).build();
    }

    // S3Client는 인터페이스라 listObjectsV2Paginator(...)가 기본(default) 메서드인데, Mockito mock은
    // 기본 메서드를 실제로 실행하지 않고 null을 반환한다 - 그래서 진짜 ListObjectsV2Iterable을 만들어 반환하도록
    // 명시적으로 stub하고, 그 이터러블이 내부적으로 호출하는 listObjectsV2(...)를 목 처리한다.
    private void stubListing(S3Object... objects) {
        ListObjectsV2Response response = ListObjectsV2Response.builder().contents(objects).isTruncated(false).build();
        given(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).willReturn(response);
        given(s3Client.listObjectsV2Paginator(any(ListObjectsV2Request.class)))
                .willReturn(new ListObjectsV2Iterable(s3Client, ListObjectsV2Request.builder()
                        .bucket("review-images").prefix("reviews/").build()));
    }

    // DB에 참조가 없고 유예 시간(60분)보다 오래된 오브젝트는 삭제되는지 검증
    @Test
    void cleanupOrphanedImages_orphanedAndOldEnough_deletesObject() {
        Instant oldEnough = Instant.now().minus(90, ChronoUnit.MINUTES);
        stubListing(object("reviews/orphan.jpg", oldEnough));
        given(reviewRepository.existsByImageUrl("reviews/orphan.jpg")).willReturn(false);

        job.cleanupOrphanedImages();

        verify(s3Client).deleteObject(DeleteObjectRequest.builder()
                .bucket("review-images").key("reviews/orphan.jpg").build());
    }

    // DB에 참조가 있는 오브젝트는 오래됐어도 삭제되지 않는지 검증
    @Test
    void cleanupOrphanedImages_referencedByReview_doesNotDeleteObject() {
        Instant oldEnough = Instant.now().minus(90, ChronoUnit.MINUTES);
        stubListing(object("reviews/linked.jpg", oldEnough));
        given(reviewRepository.existsByImageUrl("reviews/linked.jpg")).willReturn(true);

        job.cleanupOrphanedImages();

        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    // 참조는 없지만 아직 유예 시간 이내(막 업로드된)면 삭제되지 않는지 검증
    // - 업로드 직후~리뷰 생성 API 호출 사이의 정상적인 시간차와 혼동하지 않기 위한 안전장치
    @Test
    void cleanupOrphanedImages_orphanedButTooRecent_doesNotDeleteObject() {
        Instant justUploaded = Instant.now().minus(1, ChronoUnit.MINUTES);
        stubListing(object("reviews/just-uploaded.jpg", justUploaded));

        job.cleanupOrphanedImages();

        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
        verify(reviewRepository, never()).existsByImageUrl(any());
    }
}

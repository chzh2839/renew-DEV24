package com.dev24.bookstore.review.schedule;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.dev24.bookstore.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;

// presigned URL로 업로드는 성공했지만 그 뒤 POST /api/reviews가 실패해(또는 클라이언트가 중간에 포기해)
// 어떤 Review에도 연결되지 않은 채 남은 SeaweedFS 오브젝트를 주기적으로 정리한다.
//
// SeaweedFS 버킷 lifecycle 정책으로 처리하려 했으나, lifecycle expiration에 아직 안 고쳐진 버그가 있어
// (prefix 조건에 걸리는 파일 중 만료된 것만이 아니라 전부 삭제 - GitHub seaweedfs/seaweedfs#6619, 2025-03-11 open)
// 대신 우리 코드가 전적으로 통제하는 이 스케줄러로 대체했다.
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.storage.orphan-cleanup.enabled", havingValue = "true")
public class OrphanedReviewImageCleanupJob {

    private static final String PREFIX = "reviews/";

    private final S3Client s3Client;
    private final ReviewRepository reviewRepository;

    @Value("${app.storage.bucket}")
    private String bucket;
    @Value("${app.storage.orphan-cleanup.min-age-minutes}")
    private long minAgeMinutes;

    // 업로드 직후~리뷰 생성 API 호출 사이의 정상적인 시간차(수 초~수십 초)와 혼동하지 않기 위해,
    // 일정 시간(기본 60분) 이상 지난 오브젝트만 삭제 후보로 본다.
    @Scheduled(fixedDelayString = "${app.storage.orphan-cleanup.fixed-delay-ms}",
            initialDelayString = "${app.storage.orphan-cleanup.fixed-delay-ms}")
    public void cleanupOrphanedImages() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(minAgeMinutes));
        ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucket).prefix(PREFIX).build();

        int deleted = 0;
        for (S3Object object : s3Client.listObjectsV2Paginator(request).contents()) {
            if (object.lastModified().isBefore(cutoff) && !reviewRepository.existsByImageUrl(object.key())) {
                s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(object.key()).build());
                deleted++;
            }
        }
        if (deleted > 0) {
            log.info("고아 리뷰 이미지 {}건 정리", deleted);
        }
    }
}

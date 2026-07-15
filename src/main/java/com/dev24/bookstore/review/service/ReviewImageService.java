package com.dev24.bookstore.review.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dev24.bookstore.review.controller.response.PresignedUploadResponse;

import lombok.RequiredArgsConstructor;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Service
@RequiredArgsConstructor
public class ReviewImageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${app.storage.bucket}")
    private String bucket;
    @Value("${app.storage.presigned-url-expiration-seconds}")
    private long presignedUrlExpirationSeconds;

    // 확장자 화이트리스트는 PresignedUploadRequest의 @Pattern에서 이미 막으므로,
    // 여기 도달한 fileName은 이미지 확장자를 가진 것으로 가정한다.
    public PresignedUploadResponse issuePresignedUploadUrl(String fileName) {
        ensureBucketExists(); // 버킷 확인/생성
        String objectKey = "reviews/" + UUID.randomUUID() + extractExtension(fileName);

        // 서명된 URL 계산 (네트워크 호출 없음, 순수 로컬 서명 계산)
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(builder -> builder
                .signatureDuration(Duration.ofSeconds(presignedUrlExpirationSeconds))
                .putObjectRequest(PutObjectRequest.builder().bucket(bucket).key(objectKey).build()));

        return new PresignedUploadResponse(presignedRequest.url().toString(), objectKey);
    }

    // 최초 업로드 요청 시점에만 확인한다(앱 기동 시점이 아니므로 스토리지가 없는 로컬 실행/테스트엔 영향 없음) -
    // NatsConfig.ensureStreamExists()와 동일한 원칙, 이미 있으면 그대로 둔다(idempotent).
    private void ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        }
    }

    private String extractExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.'));
    }
}

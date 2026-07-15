package com.dev24.bookstore.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.dev24.bookstore.review.controller.response.PresignedUploadResponse;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

// S3Presigner는 실제 네트워크 호출 없이 로컬에서 서명만 계산하므로(presign 자체가 HTTP 요청을 안 보냄),
// mock이 아니라 더미 자격증명/엔드포인트로 만든 진짜 인스턴스를 써서 실제 SDK 동작을 그대로 검증한다.
// 실제 네트워크 호출이 필요한 S3Client(버킷 확인/생성)만 mock 처리.
@ExtendWith(MockitoExtension.class)
class ReviewImageServiceTest {

    @Mock
    private S3Client s3Client;

    private ReviewImageService reviewImageService;

    @BeforeEach
    void setUp() {
        S3Presigner s3Presigner = S3Presigner.builder()
                .region(Region.US_EAST_1)
                .endpointOverride(URI.create("http://localhost:9000"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test-access-key", "test-secret-key")))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
        reviewImageService = new ReviewImageService(s3Client, s3Presigner);
        ReflectionTestUtils.setField(reviewImageService, "bucket", "review-images");
        ReflectionTestUtils.setField(reviewImageService, "presignedUrlExpirationSeconds", 300L);
    }

    // 버킷이 이미 있으면 createBucket을 호출하지 않고, objectKey가 reviews/로 시작하며 원본 확장자를 유지하고,
    // uploadUrl에 버킷/키가 포함된 presigned URL이 발급되는지 검증
    @Test
    void issuePresignedUploadUrl_bucketExists_returnsPresignedUrlWithoutCreatingBucket() {
        given(s3Client.headBucket(any(HeadBucketRequest.class))).willReturn(HeadBucketResponse.builder().build());

        PresignedUploadResponse response = reviewImageService.issuePresignedUploadUrl("photo.jpg");

        assertThat(response.objectKey()).startsWith("reviews/").endsWith(".jpg");
        assertThat(response.uploadUrl()).contains("review-images").contains(response.objectKey());
        verify(s3Client, never()).createBucket(any(CreateBucketRequest.class));
    }

    // 버킷이 없으면(NoSuchBucketException) createBucket이 호출되는지 검증
    @Test
    void issuePresignedUploadUrl_bucketMissing_createsBucket() {
        willThrow(NoSuchBucketException.builder().message("no such bucket").build())
                .given(s3Client).headBucket(any(HeadBucketRequest.class));

        reviewImageService.issuePresignedUploadUrl("photo.png");

        verify(s3Client).createBucket(any(CreateBucketRequest.class));
    }
}

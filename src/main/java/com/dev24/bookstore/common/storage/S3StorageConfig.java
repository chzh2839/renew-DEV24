package com.dev24.bookstore.common.storage;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

// SeaweedFS(S3 호환 오브젝트 스토리지) 연결 - 포토 리뷰 이미지 업로드용(review/service/ReviewImageService 참고).
// 왜 SeaweedFS인지는 MODERNIZATION_PLAN.md 2절 참고.
@Configuration
public class S3StorageConfig {

    // app -> 스토리지 서버 대 서버 호출(버킷 확인/생성)에 쓰는 내부 엔드포인트.
    @Bean
    public S3Client s3Client(
            @Value("${app.storage.endpoint}") String endpoint,
            @Value("${app.storage.access-key}") String accessKey,
            @Value("${app.storage.secret-key}") String secretKey) {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.US_EAST_1) // S3 호환 스토리지엔 리전 개념이 없지만 SDK가 필수로 요구해 더미 값
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(pathStyleConfig())
                // SDK 2.30부터 PutObject/GetObject에 기본으로 체크섬을 계산/검증하는데(WHEN_SUPPORTED),
                // SeaweedFS는 이 체크섬 응답 방식을 온전히 지원하지 않아 GetObject가 예외 없이 빈 바이트를
                // 반환하는 문제가 있었다(리뷰 이미지 검증이 항상 실패로 오판). API가 필수로 요구할 때만
                // 체크섬을 쓰도록 낮춰 이 호환성 문제를 피한다.
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .build();
    }

    // presigned URL엔 여기 지정한 엔드포인트가 그대로 박힌다 - 브라우저/Postman 등 "호스트 머신"에서 접근할
    // 주소여야 하므로, app-SeaweedFS 컨테이너 간 통신에 쓰는 내부 호스트명(seaweedfs:9000)이 아니라
    // 포트 매핑된 공개 주소(localhost:9000)를 별도 프로퍼티로 받는다 - 헷갈리기 쉬운 포인트라 명시.
    @Bean
    public S3Presigner s3Presigner(
            @Value("${app.storage.public-endpoint}") String publicEndpoint,
            @Value("${app.storage.access-key}") String accessKey,
            @Value("${app.storage.secret-key}") String secretKey) {
        return S3Presigner.builder()
                .endpointOverride(URI.create(publicEndpoint))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(pathStyleConfig())
                .build();
    }

    // 가상 호스팅 스타일(버킷명.호스트) 대신 경로 스타일(호스트/버킷명) 사용 - SeaweedFS/MinIO 계열 S3 호환
    // 스토리지의 공통 요구사항.
    private S3Configuration pathStyleConfig() {
        return S3Configuration.builder().pathStyleAccessEnabled(true).build();
    }
}

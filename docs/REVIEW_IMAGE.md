# 포토 리뷰 이미지 업로드 가이드 (SeaweedFS, presigned URL)

리뷰에 사진을 첨부하는 흐름이다. 클라이언트가 이미지 바이트를 스프링 서버에 직접 올리지 않고, S3 호환 오브젝트 스토리지(SeaweedFS)에 **presigned URL로 직접** 업로드한다.<br>
`com.dev24.bookstore.review` 패키지(`service`/`event`/`schedule`/`controller`)와 `com.dev24.bookstore.common.storage`가 핵심이다.

## 왜 SeaweedFS인가

원래 MinIO를 검토했으나, 실제로 붙이기 전에 확인해보니 **공식 `minio/minio` Docker 이미지 저장소가 아카이브되어 더 이상 보안 패치가 나오지 않는다**. 그래서 활발히 유지보수되는 S3 호환 대안을 비교했다(자세한 근거는 `MODERNIZATION_PLAN.md` 2절 참고):

| 후보 | 제외 이유 |
|---|---|
| Garage | 노드 1개짜리 로컬 개발용으로도 `garage layout` CLI로 클러스터 부트스트랩이 필요 — `docker-compose up` 한 번으로 끝나야 하는 이 프로젝트 철학과 안 맞음 |
| RustFS | 신생 프로젝트라 운영 이력이 짧음 |
| LocalStack | AWS를 흉내내는 테스트용 목(mock) 도구 성격 — 실제 영속 스토리지로 쓰기엔 안 맞음 |
| Ceph/RGW | 멀티 데몬 클러스터 구조라 컨테이너별 `mem_limit`로 가볍게 유지한다는 원칙과 부딪힘 |
| **SeaweedFS** | 활발히 유지보수됨, `weed server -s3` 단일 프로세스로 부트스트랩 없이 단순하게 뜸, S3 API(presigned URL/SigV4 포함) 호환, MinIO 은퇴 이후 Kubeflow Pipelines가 채택할 만큼 실전 검증됨 |

## 한눈에 보는 전체 흐름

포토 리뷰 등록은 API를 **3번** 호출한다 — 이 중 2번째는 우리 백엔드가 아니라 스토리지로 직접 나가는 요청이다.

```
1) POST /api/reviews/presigned-url   [우리 API]
        │  ReviewController.issuePresignedUploadUrl()
        │  → ReviewImageService.issuePresignedUploadUrl(fileName)
        │      - 버킷 없으면 생성(idempotent)
        │      - S3Presigner로 서명된 PUT URL 계산(네트워크 호출 없음, 순수 로컬 서명)
        ▼
    { uploadUrl, objectKey } 응답
        │
        ▼
2) PUT {uploadUrl}   [우리 API 아님 - 클라이언트가 SeaweedFS로 직접]
        │  이미지 바이트가 스프링 서버를 거치지 않음(앱 인스턴스 부하 없음)
        ▼
3) POST /api/reviews  { ..., "imageUrl": objectKey }   [우리 API]
        │  ReviewController.create() → ReviewCommandService.createReview()
        │  → Review 저장(imageUrl 포함) 커밋
        │  → imageUrl이 있으면 ApplicationEventPublisher로 ReviewImageUploadedEvent 발행(스프링 내부 이벤트, NATS 아님)
        ▼
    (커밋 후, 별도 스레드) ReviewImageValidationListener.handle()
        │  스토리지에서 실제 바이트를 읽어 매직 바이트로 진짜 이미지인지 검증
        ├─ 진짜 이미지 → 그대로 둠
        └─ 가짜/읽기 실패 → 오브젝트 삭제 + Review.imageUrl을 null로 되돌림(review.clearImage())
```

| 컴포넌트 | 역할 | 파일 |
|---|---|---|
| `S3StorageConfig` | `S3Client`(서버-스토리지 내부 통신용)/`S3Presigner`(클라이언트가 접근할 공개 URL 서명용) 빈 | `common/storage/S3StorageConfig.java` |
| `ReviewImageService` | presigned PUT URL 발급, 버킷 lazy 생성 | `review/service/ReviewImageService.java` |
| `ReviewCommandService` | 리뷰 저장 후 `ReviewImageUploadedEvent` 발행(포토 리뷰일 때만) | `review/service/ReviewCommandService.java` |
| `ImageMagicBytesValidator` | 파일 확장자가 아닌 실제 바이트 시그니처로 이미지 여부 판정 | `common/storage/ImageMagicBytesValidator.java` |
| `ReviewImageValidationListener` | 커밋 후 비동기로 업로드 콘텐츠 검증, 가짜면 정리 | `review/event/ReviewImageValidationListener.java` |
| `AsyncConfig` | 검증용 전용 스레드풀(`reviewImageValidationExecutor`) | `common/config/AsyncConfig.java` |
| `OrphanedReviewImageCleanupJob` | 리뷰에 연결 안 된 채 남은 오브젝트를 주기적으로 정리 | `review/schedule/OrphanedReviewImageCleanupJob.java` |
| `SchedulingConfig` | `@EnableScheduling` | `common/config/SchedulingConfig.java` |

## 단계별 순서

### 1. 의존성 추가 (`build.gradle.kts`)
```kotlin
implementation(platform("software.amazon.awssdk:bom:2.47.6"))
implementation("software.amazon.awssdk:s3")
```
AWS SDK v2는 서브모듈(s3/auth/regions 등) 버전을 한 번에 맞추는 게 표준 관례라 BOM을 쓴다.

### 2. docker-compose에 SeaweedFS 추가
```yaml
seaweedfs:
  image: chrislusf/seaweedfs:4.39
  # -s3.port을 명시하지 않으면 S3 API가 9000이 아닌 8333에 바인딩된다(실제로 붙여보고 확인함) -
  # 관례상 포트(9000)와 이 프로젝트 나머지 설정을 맞추기 위해 명시적으로 지정.
  command: ["server", "-dir=/data", "-filer", "-s3", "-s3.port=9000", "-s3.config=/etc/seaweedfs/s3-identities.json"]
  volumes:
    - ./docker/seaweedfs/s3-identities.json:/etc/seaweedfs/s3-identities.json:ro
    - seaweedfsdata:/data
  ports:
    - "9000:9000" # S3 API
    - "8888:8888" # filer HTTP - 로컬에서 업로드된 파일을 직접 확인할 때 씀(아래 "업로드된 이미지 확인하는 방법" 참고)
  # localhost는 컨테이너 /etc/hosts에서 ::1(IPv6)이 127.0.0.1보다 먼저 오는데 SeaweedFS는 IPv4만 바인딩해서
  # wget이 IPv6로 먼저 붙었다 실패한다(실제로 붙여보고 확인함) - 127.0.0.1을 명시해서 우회.
  healthcheck:
    test: ["CMD", "wget", "--spider", "-q", "http://127.0.0.1:9333/cluster/status"]
    interval: 5s
    timeout: 5s
    retries: 10
  mem_limit: 256m
  networks: [bookstore-net]
```
`app` 서비스엔 `depends_on.seaweedfs.condition: service_healthy` + 환경변수(아래 6번), 최상위 `volumes:`에 `seaweedfsdata:` 추가.

> ⚠️ 위 두 가지(`-s3.port=9000` 미지정, `localhost` 대신 `127.0.0.1`)는 문서나 기본 예제만 보고는 알기 어렵고, 실제로 컨테이너에 붙어서(`docker exec`, `docker logs`) 확인해야 드러나는 함정이었다. SeaweedFS를 새로 붙일 때는 반드시 헬스체크가 `healthy`로 올라오는지, 로그에 `Start Seaweed S3 API Server ... at http port 9000`이 찍히는지 직접 확인할 것.

### 3. S3 자격증명 설정 (`docker/seaweedfs/s3-identities.json`, 신규)
MinIO의 `MINIO_ROOT_USER`/`PASSWORD` 같은 env var 방식이 아니라, SeaweedFS는 `-s3.config`로 JSON 파일을 받는다:
```json
{
  "identities": [
    {
      "name": "bookstore",
      "credentials": [
        {"accessKey": "bookstore-local-dev-access-key", "secretKey": "bookstore-local-dev-secret-key-not-for-prod"}
      ],
      "actions": ["Admin"]
    }
  ]
}
```
`app.jwt.secret`과 동일한 원칙 — 로컬 전용이라 누가 봐도 가짜인 값이라 커밋해도 안전.

### 4. `S3Client`/`S3Presigner` 빈 (`common/storage/S3StorageConfig.java`)
```java
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
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    // presigned URL엔 여기 지정한 엔드포인트가 그대로 박힌다 - 브라우저/Postman 등 "호스트 머신"에서 접근할
    // 주소여야 하므로, app-SeaweedFS 컨테이너 간 통신에 쓰는 내부 호스트명(seaweedfs:9000)이 아니라
    // 포트 매핑된 공개 주소(localhost:9000)를 별도 프로퍼티로 받는다 - 헷갈리기 쉬운 포인트.
    @Bean
    public S3Presigner s3Presigner(
            @Value("${app.storage.public-endpoint}") String publicEndpoint,
            @Value("${app.storage.access-key}") String accessKey,
            @Value("${app.storage.secret-key}") String secretKey) {
        return S3Presigner.builder()
                .endpointOverride(URI.create(publicEndpoint))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }
}
```
`endpoint`(내부용)와 `public-endpoint`(외부 노출용)를 왜 분리하는지가 이 설정에서 가장 중요한 포인트다<br>
— `S3Client`는 앱이 버킷을 확인/생성할 때만 쓰이니 컨테이너 내부 네트워크(`seaweedfs:9000`)로 충분하지만, `S3Presigner`가 만드는 URL은 클라이언트(브라우저 등)가 호스트 머신에서 접근해야 하므로 포트 매핑된 주소(`localhost:9000`)여야 한다.

`S3Client`/`S3Presigner` 둘 다 빈 생성 시점엔 실제 연결을 안 한다(presign은 로컬 서명 계산일 뿐) — `JavaMailSender`와 동일한 이유로 `app.nats.enabled` 같은 온오프 플래그가 필요 없다.

### 5. presigned URL 발급 서비스/DTO/엔드포인트
```java
// review/service/ReviewImageService.java
@Service
@RequiredArgsConstructor
public class ReviewImageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${app.storage.bucket}")
    private String bucket;
    @Value("${app.storage.presigned-url-expiration-seconds}")
    private long presignedUrlExpirationSeconds;

    public PresignedUploadResponse issuePresignedUploadUrl(String fileName) {
        ensureBucketExists();
        String objectKey = "reviews/" + UUID.randomUUID() + extractExtension(fileName);

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(builder -> builder
                .signatureDuration(Duration.ofSeconds(presignedUrlExpirationSeconds))
                .putObjectRequest(PutObjectRequest.builder().bucket(bucket).key(objectKey).build()));

        return new PresignedUploadResponse(presignedRequest.url().toString(), objectKey);
    }

    // 최초 업로드 요청 시점에만 확인한다(앱 기동 시점이 아니므로 스토리지 없는 로컬 실행/테스트엔 영향 없음) -
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
```
- `PresignedUploadRequest(@NotBlank @Pattern(regexp = "(?i).+\\.(jpg|jpeg|png|gif|webp)$") String fileName)` — 확장자 화이트리스트(내용 검증은 아래 7번에서 별도로).
- `PresignedUploadResponse(String uploadUrl, String objectKey)`.
- `ReviewController`:
```java
@PostMapping("/presigned-url")
@PreAuthorize("hasRole('CUSTOMER')")
public ApiResponse<PresignedUploadResponse> issuePresignedUploadUrl(@Valid @RequestBody PresignedUploadRequest request) {
    return ApiResponse.success(reviewImageService.issuePresignedUploadUrl(request.fileName()));
}
```
로그인만 확인하면 되고 소유자 검증 대상이 없다(익명 업로드로 스토리지가 남용되는 것만 막으면 됨) — `loginId`를 서비스에 넘길 필요 없음.

### 6. `application.properties` / docker-compose 환경변수
```properties
app.storage.endpoint=http://localhost:9000
app.storage.public-endpoint=http://localhost:9000
app.storage.access-key=bookstore-local-dev-access-key
app.storage.secret-key=bookstore-local-dev-secret-key-not-for-prod
app.storage.bucket=review-images
app.storage.presigned-url-expiration-seconds=300
```
```yaml
# docker-compose.yml app 서비스
APP_STORAGE_ENDPOINT: http://seaweedfs:9000
APP_STORAGE_PUBLIC_ENDPOINT: http://localhost:9000
APP_STORAGE_ACCESS_KEY: bookstore-local-dev-access-key
APP_STORAGE_SECRET_KEY: bookstore-local-dev-secret-key-not-for-prod
APP_STORAGE_BUCKET: review-images
```

### 7. 업로드 후 비동기 콘텐츠 검증 — 왜 NATS가 아니라 `@Async`인가

presigned URL 방식의 알려진 약점:<br>
서버가 업로드 시점에 이미지 바이트를 직접 못 본다(파일명 확장자만 검증). 그래서 리뷰가 만들어진 뒤, 스토리지에서 실제 바이트를 읽어 진짜 이미지가 맞는지 확인한다.

이 프로젝트는 이미 "커밋 후 비동기 처리"에 NATS JetStream(`OrderCompletedEvent`/`LowStockEvent`, `docs/NATS.md`)을 쓰고 있지만, 이번엔 같은 패턴을 쓰지 않는다.<br>
NATS를 쓴 이유는 적립금 지급/재고 알림처럼 **앱이 재시작돼도 반드시 나중에 재전달돼야 하는(at-least-once) 정합성이 중요한 이벤트**였기 때문이다.<br>
이미지 내용 검증은 최선 노력(best-effort)이면 충분하고 — 검증 하나를 놓쳐도(가짜 파일이 잠깐 더 오래 남는 정도) 업무적으로 치명적이지 않다. 그래서 NATS 스트림/컨슈머를 또 만드는 대신, 스프링 내장 `ApplicationEventPublisher` + `@Async`(전용 스레드풀)로 처리한다.

**a) 전용 스레드풀 (`common/config/AsyncConfig.java`, 신규)**
```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "reviewImageValidationExecutor")
    public Executor reviewImageValidationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("review-image-validation-");
        executor.initialize();
        return executor;
    }
}
```

**b) 이벤트 페이로드 (`review/event/ReviewImageUploadedEvent.java`, 신규)**
```java
public record ReviewImageUploadedEvent(Long reviewId, String objectKey) {}
```

**c) `ReviewCommandService`에서 저장 직후 발행** (`createReview()`/`updateReview()` 공통)
```java
private void publishImageUploadedEventIfPresent(Review review) {
    if (review.getImageUrl() != null) {
        applicationEventPublisher.publishEvent(new ReviewImageUploadedEvent(review.getId(), review.getImageUrl()));
    }
}
```
포토 리뷰일 때만(텍스트 리뷰는 검증할 파일이 없으니 스킵) `save()`/`update()` 직후 호출.

**d) 매직 바이트 검증기 (`common/storage/ImageMagicBytesValidator.java`, 신규)**
```java
@Component
public class ImageMagicBytesValidator {

    public boolean isValidImage(byte[] content, String objectKey) {
        String extension = extractExtension(objectKey);
        return switch (extension) {
            case "jpg", "jpeg" -> startsWith(content, 0xFF, 0xD8, 0xFF);
            case "png" -> startsWith(content, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
            case "gif" -> startsWith(content, 'G', 'I', 'F', '8');
            case "webp" -> content.length >= 12 && startsWith(content, 'R', 'I', 'F', 'F')
                    && matchesAt(content, 8, 'W', 'E', 'B', 'P');
            default -> false;
        };
    }
    // startsWith/matchesAt: 바이트 배열 접두/특정 오프셋 비교 헬퍼(private)
}
```
`ImageIO.read(...)`로 디코딩을 시도하는 방식도 검토했지만, JDK 기본 `ImageIO`는 WebP를 지원하지 않아(플러그인 필요) 정상 webp 파일도 검증 실패로 오판한다 — 그래서 포맷별 매직 바이트 직접 비교를 택했다(추가 의존성 불필요).

**e) 검증 리스너 (`review/event/ReviewImageValidationListener.java`, 신규)**
```java
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewImageValidationListener {

    private final S3Client s3Client;
    private final ImageMagicBytesValidator imageMagicBytesValidator;
    private final ReviewRepository reviewRepository;

    @Value("${app.storage.bucket}")
    private String bucket;

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
```
`Review.clearImage()`(신규, `Review.update()`와 동일 원칙 — 검증은 서비스가, 엔티티는 순수 데이터 홀더):
```java
public void clearImage() {
    this.imageUrl = null;
    this.type = ReviewType.TEXT;
}
```

> ⚠️ **함정 1 — self-invocation**: `ReviewCommandService` 안에 `@Async` 검증 메서드를 두면 안 된다.<br>
> 같은 클래스 안에서 자기 메서드를 직접 호출하면 프록시를 안 타 `@Async`가 조용히 무시된다(`docs/NATS.md`에 정리된 `@Transactional` self-invocation 문제와 동일한 함정) — 그래서 `ReviewImageValidationListener`를 별도 빈으로 분리했다.
>
> ⚠️ **함정 2 — `@TransactionalEventListener(AFTER_COMMIT)` + 일반 `@Transactional` 조합 금지**:<br>
> 처음엔 `handle()`에 평범한 `@Transactional`을 붙였는데, **앱 기동 자체가 실패했다**(`@TransactionalEventListener method must not be annotated with @Transactional unless when declared as REQUIRES_NEW or NOT_SUPPORTED`).<br>
> AFTER_COMMIT 시점엔 원본 트랜잭션이 이미 끝나있어 `REQUIRED`로는 참여할 트랜잭션이 없기 때문 — `@Transactional(propagation = Propagation.REQUIRES_NEW)`로 새 트랜잭션을 명시적으로 열어야 `clearInvalidImage()`의 더티체킹이 반영된다.

### 8. 고아 이미지 정리 — 왜 SeaweedFS lifecycle이 아니라 앱 스케줄러인가

presigned URL 업로드(2단계)까지 성공했는데 `POST /api/reviews`(3단계)가 실패하면, 스토리지엔 올라갔지만 DB 어디에도 참조 안 된 "고아 오브젝트"가 남는다.<br>
원래 SeaweedFS 버킷 lifecycle 정책(N일 지난 오브젝트 자동 만료)으로 해결하려 했으나, **실제로 붙이기 전에 조사해보니 SeaweedFS의 lifecycle expiration에 아직 안 고쳐진 심각한 버그가 있었다**(GitHub `seaweedfs/seaweedfs#6619`, 2025-03-11 open) — prefix 조건에 걸리는 파일 중 "만료된 것만"이 아니라 **전부** 삭제해버린다. 그대로 걸면 정상적으로 리뷰에 연결된 사진까지 같이 날아갈 위험이 있어, 대신 **앱이 직접 소유하는 스케줄러**로 대체했다.

**a) 스케줄링 활성화 (`common/config/SchedulingConfig.java`, 신규)**
```java
@Configuration
@EnableScheduling
public class SchedulingConfig {}
```

**b) DB 참조 여부 조회 (`review/repository/ReviewRepository.java`)**
```java
boolean existsByImageUrl(String imageUrl);
```

**c) 정리 잡 (`review/schedule/OrphanedReviewImageCleanupJob.java`, 신규)**
```java
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
```
판단 기준은 "N분 이상 지났는데 DB에 참조가 없으면" 삭제 — 업로드 직후~리뷰 생성 API 호출 사이의 정상적인 시간차(수 초~수십 초)와 착오로 혼동하지 않기 위해 유예 시간(기본 60분)을 둔다.

**d) 게이팅 이유**:<br>
`@Scheduled(fixedDelay=...)`는 `initialDelay`를 안 주면 앱 기동 직후 즉시 한 번 실행되는데, 게이팅 없이 두면 `@SpringBootTest` 전체 컨텍스트를 띄우는 다른 모든 테스트(`AuthIntegrationTest` 등)에서 SeaweedFS가 없는 상태로 이 잡이 실행돼 불필요한 예외/노이즈가 생긴다. `app.nats.enabled`/`app.book-seed.enabled`와 동일한 패턴으로 기본값을 꺼둔다.

**e) 프로퍼티**
```properties
app.storage.orphan-cleanup.enabled=false
app.storage.orphan-cleanup.fixed-delay-ms=3600000
app.storage.orphan-cleanup.min-age-minutes=60
```
```yaml
# docker-compose.yml app 서비스
APP_STORAGE_ORPHAN_CLEANUP_ENABLED: "true"
```

## 업로드된 이미지 확인하는 방법

MinIO와 달리 SeaweedFS는 별도 웹 콘솔이 없다. 대신 filer(파일시스템 계층)가 자체 HTTP 파일 브라우저를 제공한다 — 위 2번(docker-compose)에서 `8888` 포트를 매핑해뒀으면 브라우저로 바로 확인 가능하다. 실제로 아래 세 방법 모두 로컬에서 검증했다.

**1) 브라우저로 목록/열람 (가장 간단, 포트 매핑 필요)**<br>
`http://localhost:8888/buckets/review-images/reviews/` 접속 — 업로드된 파일 목록이 뜨고, 각 파일을 클릭하면 이미지가 그대로 열린다. SeaweedFS는 S3 버킷을 filer 네임스페이스의 `/buckets/<버킷명>/` 경로에 그대로 매핑한다.

**2) CLI로 목록/크기 확인 (`weed shell`, 포트 매핑 불필요)**
```bash
echo "fs.ls -l /buckets/review-images/reviews" | docker exec -i dev24-seaweedfs-1 weed shell -filer=localhost:8888
```
결과 예시:
```
-rw-rw----   1 root root 1563159 /buckets/review-images/reviews/3c92e8cb-5c87-46e1-9373-2bcbf7658428.jpg
```
`objectKey`(`reviews/{UUID}.확장자`)나 `Review.image_url` 값과 그대로 대조하면 된다.

**3) 실제 바이트가 진짜 이미지인지 확인 (매직 바이트 직접 확인)**
```bash
docker exec dev24-seaweedfs-1 wget -qO /tmp/downloaded.jpg \
  http://127.0.0.1:8888/buckets/review-images/reviews/3c92e8cb-5c87-46e1-9373-2bcbf7658428.jpg
docker exec dev24-seaweedfs-1 sh -c "head -c 16 /tmp/downloaded.jpg | xxd"
```
JPEG라면 `ffd8ff...`로 시작해야 한다(실제로 확인함: `00000000: ffd8 ffe0 0010 4a46 4946 ...`). 호스트로 꺼내서 직접 열어보고 싶으면 `docker cp dev24-seaweedfs-1:/tmp/downloaded.jpg ./downloaded.jpg`.

**DB와 대조**: `SELECT id, image_url, type FROM review WHERE image_url IS NOT NULL;` — `image_url`이 위 목록의 파일과 일치하면 정상 연결된 것이고, `ReviewImageValidationListener`가 가짜 이미지로 판정했다면 이 값이 `null`로 되돌아가 있고(동시에 스토리지에서도 삭제됨) 목록 1)/2)에서도 그 파일이 사라져 있다.

## 테스트 시 주의할 점 — `S3Client.listObjectsV2Paginator(...)` Mockito 함정

`S3Client`는 인터페이스라 `listObjectsV2Paginator(...)`가 기본(default) 메서드인데, **Mockito mock은 기본 메서드를 실제로 실행하지 않고 `null`을 반환한다.** 그래서 `given(s3Client.listObjectsV2(...)).willReturn(...)`만 stub하면 `NullPointerException`이 난다. 진짜 `ListObjectsV2Iterable` 인스턴스를 만들어 반환하도록 명시적으로 stub해야 한다:
```java
given(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).willReturn(response);
given(s3Client.listObjectsV2Paginator(any(ListObjectsV2Request.class)))
        .willReturn(new ListObjectsV2Iterable(s3Client, ListObjectsV2Request.builder()
                .bucket("review-images").prefix("reviews/").build()));
```

## 테스트

- **단위 테스트**:
  - `ImageMagicBytesValidatorTest` — 4개 포맷(jpg/png/gif/webp) 정상 시그니처 통과, 비이미지 바이트 거부, 확장자-시그니처 불일치 거부.
  - `ReviewImageServiceTest` — 버킷 존재/미존재(생성) 분기, 발급된 `objectKey`가 `reviews/`로 시작하고 원본 확장자를 유지하는지. `S3Presigner`는 실제 네트워크 호출 없이 로컬 서명만 계산하므로 mock 대신 더미 자격증명으로 만든 진짜 인스턴스를 쓴다.
  - `ReviewImageValidationListenerTest` — 유효한 이미지(정리 없음) / 무효한 이미지(오브젝트 삭제+리뷰 정리) / 읽기 실패(리뷰만 정리).
  - `ReviewCommandServiceTest`에 케이스 추가 — 포토 리뷰 생성/수정 시 `ReviewImageUploadedEvent` 발행 여부(`ArgumentCaptor`).
  - `OrphanedReviewImageCleanupJobTest` — 고아+충분히 오래됨(삭제) / DB 참조 있음(보존) / 고아지만 아직 유예시간 이내(보존).
- **컨트롤러 슬라이스**: `ReviewControllerTest`에 presigned URL 발급 케이스 추가 — 고객 인증 시 성공(200), 비로그인 403+A004, 확장자 검증 실패 400+C001.
- **수동 확인**: `docker compose up -d`로 SeaweedFS 포함 전체 스택 기동 후, `http/bookstore.http`의 장바구니→구매→(6~9번) 리뷰 작성 흐름을 순서대로 실행 — presigned URL 발급 → `PUT`으로 실제 이미지 업로드 → 리뷰 생성 → 잠시 후 `SELECT image_url FROM review WHERE id=...`로 유지되는지(진짜 이미지였다면) 확인.

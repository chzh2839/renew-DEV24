# 테스트 커버리지 감사 (Phase 7)

핵심 대표 모듈 4개(인증/도서 카탈로그/장바구니·구매·재고/리뷰)의 현재 테스트를 클래스 단위로 대조해 정리한 문서다. 목적은 전수 커버리지 수치 확보(JaCoCo)가 아니라 **"뭐가 테스트돼 있고, 뭐가 왜 안 돼 있는지"를 한눈에 파악**하는 것이다.

## 모듈별 매핑

### 인증(auth)

| 클래스 | 테스트 | 유형 |
|---|---|---|
| `AuthController` | `AuthControllerTest` | 컨트롤러 슬라이스(`@WebMvcTest`) |
| `AuthController` 전체 흐름 | `AuthIntegrationTest` | 통합(`@SpringBootTest`, Testcontainers) |
| `CustomerRepository`/`AdminRepository` | `AuthRepositoryTest`(신규) | 리포지토리 슬라이스(`@DataJpaTest`, Testcontainers) |
| `AccessTokenBlacklist` | `AccessTokenBlacklistTest` | 단위 |
| `BearerTokenResolver` | `BearerTokenResolverTest`(신규) | 단위 |
| `JwtAuthenticationFilter` | `JwtAuthenticationFilterTest` | 단위 |
| `JwtTokenProvider` | `JwtTokenProviderTest` | 단위 |
| `RefreshTokenStore` | `RefreshTokenStoreTest` | 단위 |
| `AdminService` | `AdminServiceTest` | 단위(Mockito) |
| `CustomerService` | `CustomerServiceTest` | 단위(Mockito) |

### 도서 카탈로그(book)

| 클래스 | 테스트 | 유형 |
|---|---|---|
| `BookController` | `BookControllerTest` | 컨트롤러 슬라이스 |
| `KakaoBookClient` | `KakaoBookClientIntegrationTest` | 통합(외부 API) |
| `BookQueryRepository`/`Impl` | `BookQueryRepositoryTest`, `BookQueryRepositoryNPlusOneTest` | 리포지토리 슬라이스 |
| 검색 인덱스 마이그레이션 | `BookSearchIndexMigrationTest` | 통합(Testcontainers) |
| `BookCommandService` | `BookCommandServiceTest`, `BookCommandServiceCacheTest` | 단위 + 캐시 무효화 검증 |
| `BookQueryService` | `BookQueryServiceTest`, `BookQueryServiceCacheTest` | 단위 + 캐시 히트 검증 |
| `BookSeedService` | `BookSeedServiceTest` | 단위 |

`BookImageRepository`/`RatingRepository`는 커스텀 쿼리 메서드가 없는 순수 `JpaRepository<T, ID>`라 전용 테스트가 없다 — Spring Data가 이미 검증하는 프레임워크 기본 CRUD 이상의 로직이 없다.

### 장바구니/구매/재고(purchase)

| 클래스 | 테스트 | 유형 |
|---|---|---|
| `CartController` | `CartControllerTest` | 컨트롤러 슬라이스 |
| `PurchaseController` | `PurchaseControllerTest` | 컨트롤러 슬라이스 |
| `CartRepository`/`PurchaseRepository`/`PurchaseItemRepository`/`StockRepository` | `PurchaseModuleRepositoryTest` | 리포지토리 슬라이스(엔티티 매핑/제약조건) |
| `CartCommandService` | `CartCommandServiceTest` | 단위 |
| `PurchaseCommandService` | `PurchaseCommandServiceTest`, `PurchaseCommandServiceTransactionalTest` | 단위 + 동시성/트랜잭션 통합 |
| `LowStockEventConsumer` | `LowStockEventConsumerTest`, `LowStockEventIntegrationTest` | 단위 + 통합(Testcontainers NATS) |
| `OrderCompletedEventConsumer` | `OrderCompletedEventConsumerTest`, `OrderCompletedEventIntegrationTest` | 단위 + 통합(Testcontainers NATS) |
| `LowStockEventPublisher` | `LowStockEventPublisherTest`(신규) | 단위 |
| `OrderCompletedEventPublisher` | `OrderCompletedEventPublisherTest`(신규) | 단위 |

`Stock.decreaseQuantity()`처럼 엔티티에 남은 로직은 단순 데이터 변경뿐이라("검증은 서비스 책임" 원칙, `Stock.java` 주석) 전용 엔티티 테스트 없이 `PurchaseCommandServiceTest`/`PurchaseCommandServiceTransactionalTest`로 간접 검증된다.

### 리뷰(review)

| 클래스 | 테스트 | 유형 |
|---|---|---|
| `ReviewController` | `ReviewControllerTest` | 컨트롤러 슬라이스 |
| `ReviewRepository` | `ReviewRepositoryTest` | 리포지토리 슬라이스 |
| `ReviewCommandService` | `ReviewCommandServiceTest` | 단위 |
| `ReviewQueryService` | `ReviewQueryServiceTest` | 단위 |
| `ReviewImageService` | `ReviewImageServiceTest` | 단위 |
| `ReviewImageValidationListener` | `ReviewImageValidationListenerTest` | 단위 |
| `OrphanedReviewImageCleanupJob` | `OrphanedReviewImageCleanupJobTest` | 단위 |
| `ImageMagicBytesValidator` | `ImageMagicBytesValidatorTest`(`common/storage`) | 단위 |

리뷰 모듈은 가장 최근에 작성돼 커버리지가 가장 촘촘하다. `Review.clearImage()`/`update()` 등 엔티티 메서드는 다른 모듈과 동일한 원칙으로 전용 테스트 없이 서비스/리스너 테스트로 간접 검증된다.

## 왜 일부 클래스는 전용 테스트가 없는가

이 프로젝트는 일관되게 **"엔티티는 순수 데이터 홀더, 검증·분기 로직은 서비스 레이어"** 원칙을 따른다(`Review.java`, `Stock.java`, `Book.java` 주석에 반복 명시). 그래서:
- 엔티티 자체(`Book`/`Stock`/`Review`/`Cart`/`Purchase` 등)는 생성자/getter/단순 setter성 메서드만 있어 전용 단위 테스트의 실익이 낮고, 대신 리포지토리 테스트(매핑/제약조건)와 서비스 테스트(분기 로직)로 간접 검증된다.
- 커스텀 쿼리 메서드가 없는 순수 `JpaRepository<T, ID>`(`BookImageRepository`, `RatingRepository`)는 Spring Data 프레임워크가 이미 보장하는 영역이라 추가 테스트가 새로운 리스크를 검증해주지 않는다.

## 이번에 보완한 갭

전체 소스-테스트 대조 결과, 분기/에러 처리 로직이 있는데도 직접 검증하는 테스트가 없던 곳 3곳을 보완했다:

1. **`BearerTokenResolver`** — `Authorization` 헤더 파싱 유틸에 3가지 분기(정상/헤더 없음/`Bearer ` 접두사 없음)가 있었는데 이를 직접 검증하는 테스트가 하나도 없었다. → `BearerTokenResolverTest` 추가.
2. **`CustomerRepository`/`AdminRepository`의 파생 쿼리** — book/purchase/review 모듈은 각각 전용 리포지토리 테스트가 있는데 auth 모듈만 없었다(다른 테스트에서 두 리포지토리를 FK 설정용 `.save()` 헬퍼로만 썼을 뿐, `findByLoginId`/`existsByLoginId`/`findAllByAdminRole` 자체는 검증된 적이 없었음). → `AuthRepositoryTest` 추가.
3. **`OrderCompletedEventPublisher`/`LowStockEventPublisher`** — "NATS 발행 실패 시 이미 커밋된 주문/재고 처리는 되돌리지 않고 로그만 남긴다"는 트레이드오프가 주석에 문서화돼 있는데, 이 catch-and-swallow 분기를 검증하는 테스트가 없었다(컨슈머 쪽은 `LowStockEventConsumerTest`로 이미 검증되지만 발행자 쪽은 통합 테스트의 성공 경로만 돔). → `LowStockEventPublisherTest`, `OrderCompletedEventPublisherTest` 추가.

## 범위 밖

- JaCoCo 등 커버리지 수치 실측/CI 게이팅은 `MODERNIZATION_PLAN.md` Phase 7의 다음 항목("GitHub Actions 테스트 스텝 추가(+ Jacoco 선택)")에서 별도로 다룬다.

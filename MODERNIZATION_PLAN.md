# DEV24 현대화 리팩토링 로드맵

> 2020년 작성된 Spring 5(XML 설정) + MyBatis + JSP/Tiles + Oracle 기반 도서 쇼핑몰 프로젝트를, 재구성하기 위한 로드맵이다.
> 목표는 단순 최신 스택 교체가 아니라 **레거시 코드에서 실제 문제를 발견하고, 표준적인 방법으로 해결한 과정**을 보여주는 것이다.

## 1. 현재 상태 분석 (As-Is)

- **빌드/설정**: Java 8, Spring 5.0.7(순수 XML 설정, Boot 아님), MyBatis 3.4.6, Oracle(ojdbc6), Tiles3 + JSP/JSTL, log4j 1.x(EOL). `root-context.xml`에 DB 자격증명 평문(`devuser`/`dev1234`), `web.xml`에 하드코딩된 Windows 업로드 경로(`C:\uploadStorage`).
- **보안**: `Login.xml`(`WHERE c_id=... and c_passwd=...`), `Admin.xml`(`adm_passwd=...`) 모두 비밀번호를 SQL에서 평문 비교. CSRF 방어 없음. JSP 출력 대부분 `${...}`로 미이스케이프(`c:out` 사용은 5개 파일·9곳뿐) → 저장형 XSS 위험. 관리자 인증은 인터셉터 없이 컨트롤러마다 개별적으로 `session.getAttribute("adm_num")`을 체크.
- **아키텍처**: Controller(33) → Service(26, interface+impl) → DAO(24) 계층은 대체로 일관되지만 일부 모듈은 레이어 누락. `@Transactional`은 28개 ServiceImpl 중 7개에만, 그마저 메서드별 일관성 없이 적용. `@ControllerAdvice`/Bean Validation 전무, 예외는 서비스단 `try/catch` + `e.printStackTrace()`로 삼키고 숫자 상태코드 반환.
- **성능**: `Book.xml`에 실제로 미해결 상태로 남아있는 `성능개선` 주석 처리 쿼리 존재 — 이전 개발자가 `count(*) + LEFT OUTER JOIN` 성능 문제를 인지했으나 해결하지 못하고 커밋된 흔적.
- **API 설계**: `@Controller` + 선택적 `@ResponseBody` 혼재(`@RestController` 없음), 응답 포맷 통일 없음.
- **테스트**: 16개 테스트 전부 MyBatis 매퍼를 라이브 Oracle DB에 직접 붙여 검증하는 통합 테스트뿐. 단위 테스트/Mockito, 서비스·컨트롤러 테스트 없음.
- **프론트**: 빌드 파이프라인 없이 정적 리소스, jQuery 두 버전(1.12.4/3.5.1) 동시 로드.

## 2. 방향 결정 (To-Be)

1. **DB**: Oracle → PostgreSQL
2. **영속성**: MyBatis → JPA + QueryDSL
3. **API**: REST API 서버로 전환, 화면(JSP)은 최소 유지 — 백엔드 중심
4. **범위**: 전체 재작성이 아니라 핵심 대표 모듈 4개를 선별해 깊게 개선, 나머지는 레거시로 보존하고 범위 밖으로 명시
5. **추가 인프라**: Redis(캐싱 + 세션/리프레시 토큰), NATS JetStream(이벤트 발행), Nginx(리버스 프록시 + 로드밸런싱), MinIO(S3 호환 오브젝트 스토리지) 도입

기존 `DEV24Test`는 손대지 않고 `legacy/` 하위(또는 별도 브랜치)로 보존해 README의 Before 근거로 사용한다. 신규 코드는 완전히 새 Spring Boot 프로젝트로 시작해 아래 4개 모듈만 끝까지 깊게 구현한다.

인프라 요소는 막연히 추가하지 않고, 각각 특정 Phase의 구체적 문제와 연결해 "왜 썼는지" 설명 가능하게 도입한다.

- **Redis**: (a) 인증 모듈 — JWT 리프레시 토큰 저장 및 로그아웃 시 액세스 토큰 블랙리스트 처리. (b) 도서 카탈로그 — `@Cacheable` 기반 목록/상세 조회 캐싱, 캐시 히트·미스 응답시간 비교를 성능 개선 근거자료로 추가.
- **NATS JetStream**: 구매 모듈 — 재고 차감은 정합성이 중요하므로 `@Transactional`+`@Version` 낙관적 락으로 동기 처리하고, 트랜잭션 커밋 후 부가 로직(적립금 지급/알림)만 `OrderCompletedEvent`로 발행해 별도 리스너가 비동기 소비하도록 분리한다. 재고가 안전재고 이하로 떨어지면 `LowStockEvent`도 같은 방식으로 발행 — 강한 정합성이 필요한 부분과 최종적 일관성으로 충분한 부분을 구분해 설계했다는 스토리, 그리고 동일 이벤트 패턴이 두 시나리오(주문 완료/재고 부족)에 재사용됨을 보여준다.
- **Nginx**: 인프라 — app 컨테이너를 2개 이상 띄우고 Nginx를 리버스 프록시 겸 라운드로빈 로드밸런서로 앞단에 배치.
- **MinIO**: 리뷰 모듈 — 포토 리뷰 이미지 저장. 레거시는 로컬 디스크(`C:\uploadStorage`)에 저장했는데, 이 프로젝트는 app 컨테이너가 2 replica로 뜨는 구조라 컨테이너별 로컬 디스크가 서로 분리돼 있어 그대로 재현하면 안 된다(인스턴스마다 파일이 따로 쌓여 다른 인스턴스가 응답하면 404). 그렇다고 진짜 AWS S3를 쓰면 `docker-compose up` 하나로 완결되던 로컬 개발 환경이 AWS 자격증명에 의존하게 되므로, S3 호환 API를 제공하는 MinIO를 로컬 컨테이너로 띄운다 — 코드는 실제 AWS SDK/presigned URL 업로드 패턴을 그대로 쓰면서, 실 배포 시엔 엔드포인트만 S3로 바꿔 끼우면 되는 구조.

## 3. 선정한 4개 핵심 모듈

| # | 모듈 | 레거시 문제 → 개선 | 인터뷰 소구점 |
|---|---|---|---|
| 1 | **인증** (로그인+관리자) | `Login.xml`/`Admin.xml` 평문 비교, 인터셉터 없는 세션 체크 → BCrypt + Spring Security 6 + JWT, 중앙화된 권한 체크 | 보안 취약점을 스스로 발견하고 표준 방식으로 고친 스토리 |
| 2 | **도서 카탈로그** (book+bookimg+pdetail+rating) | `Book.xml`의 미해결 `성능개선` 쿼리, rownum 페이징 → QueryDSL 동적 검색 + Postgres 인덱스 + `EXPLAIN ANALYZE` before/after | 실존하던 미해결 성능 이슈를 실제로 진단·해결 |
| 3 | **장바구니/구매/재고** | `@Transactional` 누락·불일치, 재고 차감 시 동시성 미보호(오버셀 위험) → 명시적 트랜잭션 경계 + `@Version` 낙관적 락 | 트랜잭션/동시성 정합성 문제 해결 |
| 4 | **리뷰** | JSP 미이스케이프 출력(XSS), 혼재된 응답 방식, 수제 페이징 | REST API 설계 + Bean Validation + `Pageable` 표준 페이징 + 소유자 검증(`@PreAuthorize`) |

이 4개는 각각 보안 / 성능 / 트랜잭션 정합성 / API 설계라는 서로 다른 축을 대표한다.

**재고 차감에 비관적 락 대신 낙관적 락(`@Version`)을 선택한 이유** (트레이드오프):
- 비관적 락(`SELECT ... FOR UPDATE`)은 트랜잭션이 끝날 때까지 재고 row를 계속 잠가, 인기 도서(hot row)에 주문이 몰리면 요청이 줄줄이 대기하며 처리량이 떨어진다.
- 온라인 서점의 재고 차감은 "같은 도서를 노리는 두 요청이 정확히 같은 순간에 충돌"하는 경우가 드물다 — 대부분은 잠금 없이 통과되고, 마지막 재고 하나를 두고 경쟁하는 드문 순간에만 충돌이 감지되면 된다. JPA가 `UPDATE ... WHERE version = ?`로 조건부 갱신하다가 영향받은 row가 0이면 `OptimisticLockException`을 던지므로, 평소엔 락 대기 없이 빠르게 처리되고 충돌이 실제 발생하는 드문 케이스에만 재시도 비용을 지불한다.
- 분산락(Redisson 등)을 쓰지 않은 이유는 재고 차감이 단일 DB 트랜잭션 안에서 끝나는 구조라 별도 분산락 인프라가 불필요하기 때문 — 여러 서비스가 같은 재고를 공유하는 구조가 된다면 그때 고려할 대안이다.
- 한계: "선착순 한정판" 같은 초고경합(flash sale) 시나리오에서는 낙관적 락이 재시도 폭주로 비효율적일 수 있어, 그런 경우엔 비관적 락이나 Redis 기반 분산 큐잉이 더 나을 수 있다는 트레이드오프 이해도 README에 함께 적는다.

**범위 밖(레거시 유지, 포팅 안 함)**: faq, freeboard/freecmt, ne/necmt, mypage(orderhistory/refundhistory), refund, qna 등. 이유와 적용 가능한 패턴(예: freeboard는 review와 동일한 페이징/XSS 패턴 적용 가능)을 최종 README에 표로 명시한다.

## 4. 신규 기술 스택

- **Java 21(LTS) + Spring Boot 3.5.x** (Jakarta 네임스페이스 — virtual thread 등 최신 LTS 기능 언급 가능. 3.3.x는 Initializr에서 더 이상 제공되지 않아 3.x 라인 최신인 3.5.x로 진행)
- **Gradle(Kotlin DSL)** — QueryDSL Q-type 코드젠 설정이 깔끔하고 최신 Spring 진영 관례에 가까움
- **PostgreSQL 16** + **Flyway** 스키마 버전관리 (`V5__add_book_search_index.sql`처럼 성능 개선 이력을 마이그레이션으로 남김)
- **Spring Security 6 + JWT** (Stateless REST API이므로 CSRF는 불필요 — README에 근거 명시)
- 공통 응답: `ApiResponse<T>` + `ErrorCode` enum + `@RestControllerAdvice`(`GlobalExceptionHandler`)
- **springdoc-openapi** (Swagger UI)
- 테스트: **JUnit5 + Mockito + AssertJ**(서비스 단위), **Testcontainers(Postgres)**(레포지토리/통합) — 라이브 Oracle 의존 테스트를 완전히 대체
- **GitHub Actions**로 push/PR 시 빌드+테스트 자동 실행
- **Redis 7** — 리프레시 토큰/로그아웃 블랙리스트(인증 모듈) + Spring Cache 캐싱(도서 카탈로그 모듈)
- **NATS JetStream** — 단일 바이너리로 가볍게 동작(Zookeeper/별도 컨트롤러 클러스터 불필요), 구매 완료 후 적립금/알림, 안전재고 이하 시 재입고 알림 등을 비동기 이벤트로 분리(구매 모듈), 테스트는 Testcontainers(NATS 이미지 기반 GenericContainer) 사용
- **Nginx** — 리버스 프록시 + app 2 replica 앞단 라운드로빈 로드밸런싱 데모
- **MinIO** — S3 호환 오브젝트 스토리지, 리뷰 모듈의 포토 리뷰 이미지 저장. 클라이언트가 presigned URL로 MinIO에 직접 업로드(서버는 URL 발급만) — 이미지 바이트가 스프링 서버를 거치지 않아 app 인스턴스 부하가 늘지 않고, 실 배포 시엔 엔드포인트를 AWS S3로 바꿔 끼우기만 하면 되는 구조
- **Dockerfile(멀티스테이지) + docker-compose.yml**(app×2 + postgres + redis + nats + nginx + minio) — `docker-compose up` 한 번으로 전체 인프라 로컬 실행
- **컨테이너 메모리 제한** — `docker-compose.yml`에서 서비스별 `mem_limit`(또는 `deploy.resources.limits.memory`)로 최대 사용 메모리 상한 지정(예: DB(PostgreSQL) 512MB, Redis 256MB, NATS 128MB 등)

## 5. Phase 로드맵

각 Phase는 종료 시점에 동작하는 결과물을 남기는 것을 원칙으로 한다.

### Phase 0. 동결 & 준비
- 기존 `DEV24Test`를 `legacy/`로 보존
- 4개 모듈 ERD 작성(인증, 도서, 장바구니/구매/재고, 리뷰)
- 패키지 루트 `com.dev24.bookstore` 확정
- README 뼈대 작성

### Phase 1. 앱 골격 & 인프라
- Spring Boot 3.5 + Gradle 프로젝트 생성
- `ApiResponse`/`ErrorCode`/`GlobalExceptionHandler` 공통 골격
- springdoc 연동
- GitHub Actions 빌드 워크플로우
- Docker Compose에 Postgres+Flyway
- Redis
- NATS JetStream
- app 2 replica + Nginx(리버스 프록시 겸 라운드로빈 로드밸런서) 구성
- 각 컨테이너 메모리 제한(`mem_limit`) 설정 — 예: DB 512MB, Redis 256MB, NATS 128MB
- → `docker-compose up`으로 health check/Swagger 응답 확인 + Nginx를 통한 두 인스턴스 라운드로빈 접속 확인

### Phase 2. 인증 모듈
- Customer/Admin 엔티티+Role
- BCrypt 해시
- JWT 발급/검증 `SecurityFilterChain`
- `@PreAuthorize` 권한 분리
- `@Valid` 요청 DTO
- **Redis**에 리프레시 토큰 저장 및 로그아웃 시 액세스 토큰 블랙리스트 처리
- Mockito 단위 + Testcontainers(Postgres+Redis) 통합 테스트

### Phase 3. 도서 카탈로그 모듈
- `Book`/`BookImage`/`Rating` 엔티티
- `BookRepository`+QueryDSL `BookQueryRepository`(동적 검색/필터)
- `Pageable` 페이징으로 rownum 대체
- 검색 인덱스 추가 + `EXPLAIN ANALYZE` before/after 문서화
- N+1 방지
- 목록/상세 조회에 **Redis 기반 Spring Cache**(`@Cacheable`) 적용
- 캐시 히트/미스 응답시간 비교를 README 근거자료로 정리
- 수정 시 `@CacheEvict`

### Phase 4. 장바구니/구매/재고 모듈
- `Cart`/`Purchase`/`PurchaseItem`/`Stock` 엔티티
- 구매 플로우 전체 `@Transactional` 경계
- `Stock`에 `@Version` 낙관적 락으로 오버셀 방지(비관적 락 대신 낙관적 락을 택한 이유는 위 3절 참고)
- `Stock`에 **안전재고(`safetyStock`)** 필드를 추가해 구매 가능 수량을 `현재 재고 - 안전재고`로 검증
- 구매 완료 후 적립금/알림은 `OrderCompletedEvent`를 **NATS JetStream**으로 발행하고 별도 컨슈머가 비동기 소비
- 안전재고 이하 도달 시 `LowStockEvent`도 동일하게 **NATS JetStream**으로 발행
- mailpit으로 알림 메일 전송 및 전송 이력 저장 (`email_notification_history`엔티티 추가)
- 동시성 시나리오 + 안전재고 시나리오 + Testcontainers(NATS) 통합 테스트

### Phase 5. 리뷰 모듈
- `Review` 엔티티/DTO
- OWASP HTML Sanitizer
- 소유자 검증
- `Page<ReviewResponse>` 표준 페이징
- `@WebMvcTest`
- **MinIO** 연동 — docker-compose에 MinIO 컨테이너 추가, 포토 리뷰용 presigned URL 발급 API(업로드는 클라이언트가 MinIO에 직접), 발급된 오브젝트 키를 `Review.imageUrl`로 저장

### Phase 6. 최소 데모 화면
- Swagger UI + Postman/Insomnia 컬렉션을 기본 데모 경로로
- 필요시 로그인→목록→장바구니→구매→리뷰 흐름만 도는 최소 정적 페이지 하나만 추가

### Phase 7. 테스트/CI 마무리
- 4개 모듈 전체 단위/통합 테스트 커버리지 정리
- GitHub Actions 테스트 스텝 추가(+ Jacoco 선택)

### Phase 8. 문서화
- README에 아키텍처 다이어그램
- ERD
- 레거시 대비 개선점 before/after 표
- 실행 방법
- Out-of-Scope 모듈 표

### Phase 9. 선택/여유
- freeboard/qna에 리뷰 모듈 패턴 재적용
- 도서 목록 Caffeine 캐시
- Actuator+Micrometer
- 서비스 계층 스타일 통일: auth 모듈(`CustomerService`/`AdminService` 인터페이스+Impl)을 book 모듈(`BookQueryService`/`BookCommandService` 등 interface 없는 plain class) 스타일로 맞출지 검토 - 구현체가 하나뿐이라 인터페이스의 다형성 이득이 없음
- 리프레시 토큰 회전(rotation) — 재사용 감지로 탈취 대응 강화
- 재고 차감 동시성 전략 재검토: 선착순 한정판 같은 초고경합(flash sale) 시나리오에서는 낙관적 락이 재시도 폭주로 비효율적일 수 있어, 비관적 락이나 Redis 기반 원자적 카운터/분산 큐잉이 더 나을 수 있다는 트레이드오프(3절 참고) — 실제 도입 여부와, 도입한다면 전체 교체가 아니라 선착순 전용 경로로 한정할지 검토

## 6. 검증 방법

- 각 Phase 종료 시 `docker-compose up` 후 Swagger UI에서 해당 모듈 API 직접 호출로 동작 확인
- `./gradlew test`로 Mockito 단위 테스트 + Testcontainers(Postgres/Redis/NATS) 통합 테스트 통과 확인 (로컬 Docker 필요)
- 도서 카탈로그 모듈: 실제 `EXPLAIN ANALYZE` 결과를 인덱스 추가 전/후로, Redis 캐시 적용 전/후 응답시간을 함께 캡처해 README에 근거로 남김
- Nginx를 통해 반복 요청 시 두 app 인스턴스에 라운드로빈으로 분산되는지 로그로 확인
- 구매 완료 시 NATS JetStream에 `OrderCompletedEvent`가 발행되고 컨슈머가 정상 소비(적립금/알림 반영)하는지 확인
- 재고가 안전재고 임계치 이하로 떨어지는 시나리오에서 `LowStockEvent`가 발행되고, 안전재고 초과 주문은 거부되는지 확인
- 포토 리뷰 업로드 시 발급받은 presigned URL로 MinIO에 실제 저장되는지, 저장된 오브젝트 키로 이미지가 정상 조회되는지 확인
- 컨테이너별 메모리 제한 설정 후 `docker stats`로 각 컨테이너가 지정한 상한 내에서 동작하는지 확인
- GitHub Actions가 push 시 빌드+테스트를 정상 통과하는지 Actions 탭에서 확인

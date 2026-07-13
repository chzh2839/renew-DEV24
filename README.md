# DEV24 개발자 서적 쇼핑몰 — 현대화 리팩토링

원본 프로젝트(2020.10.19 ~ 2020.11.16, Spring 5 + MyBatis + JSP)를 최신 스택으로 현대화하는 프로젝트다.<br> 목표와 단계별 로드맵은 [`MODERNIZATION_PLAN.md`](./MODERNIZATION_PLAN.md)에 정리되어 있다.

## 개요

_작성 예정 (Phase 8에서 최종 정리)_

## 아키텍처

_작성 예정 — Phase 1 이후 앱 골격이 만들어지면 다이어그램 추가_

### 이벤트 브로커: Kafka 대신 NATS JetStream를 택한 이유

구매 모듈의 비동기 이벤트(`OrderCompletedEvent`, `LowStockEvent`)는 Kafka 대신 **NATS JetStream**으로 구현한다.

- **가벼운 리소스 사용**: Kafka는 JVM 기반 브로커라 실행에 최소 수백 MB~1GB 이상의 메모리가 필요하지만, NATS(JetStream 포함)는 단일 Go 바이너리로 동작해 메모리 사용량이 훨씬 적다. 컨테이너별 메모리 상한(`mem_limit`)을 두는 이 프로젝트의 로컬 개발 환경 조건과도 잘 맞는다.
- **운영 단순성**: Zookeeper나 별도 컨트롤러 클러스터 없이 컨테이너 하나로 바로 실행 가능해 로컬 데모/CI 구성이 단순하다.
- **기능 충분성**: `OrderCompletedEvent`/`LowStockEvent`는 발행-구독 기반 최종적 일관성(eventual consistency) 처리가 목적이며, JetStream의 영속 스트림 + at-least-once 전달 보장만으로 충분하다. 대용량 스트리밍·파티셔닝 같은 Kafka 고유의 강점이 필요한 워크로드는 아니다.
- **트레이드오프**: Kafka가 업계 표준으로서 생태계·인지도 면에서는 더 유리하지만, 이 프로젝트 규모에서는 자원 효율성과 운영 단순성이 더 중요하다고 판단했다.

## 기술 스택

- **Java 21(LTS) + Spring Boot 3.5.x** (Jakarta 네임스페이스 — virtual thread 등 최신 LTS 기능 언급 가능)
- **Gradle(Kotlin DSL)** — QueryDSL Q-type 코드젠 설정이 깔끔하고 최신 Spring 진영 관례에 가까움
- **PostgreSQL 16** + **Flyway** 스키마 버전관리 (`V5__add_book_search_index.sql`처럼 성능 개선 이력을 마이그레이션으로 남김)
- **Spring Security 6 + JWT** (Stateless REST API이므로 CSRF는 불필요)
- 공통 응답: `ApiResponse<T>` + `ErrorCode` enum + `@RestControllerAdvice`(`GlobalExceptionHandler`)
- **springdoc-openapi** (Swagger UI)
- 테스트: **JUnit5 + Mockito + AssertJ**(서비스 단위), **Testcontainers(Postgres)**(레포지토리/통합) — 라이브 Oracle 의존 테스트를 완전히 대체
- **GitHub Actions**로 push/PR 시 빌드+테스트 자동 실행
- **Redis 7** — 리프레시 토큰/로그아웃 블랙리스트(인증 모듈) + Spring Cache 캐싱(도서 카탈로그 모듈)
- **NATS JetStream** — 단일 바이너리로 가볍게 동작(Zookeeper/별도 컨트롤러 클러스터 불필요), 구매 완료 후 적립금/알림, 안전재고 이하 시 재입고 알림 등을 비동기 이벤트로 분리(구매 모듈), 테스트는 Testcontainers(NATS 이미지 기반 GenericContainer) 사용
- **Nginx** — 리버스 프록시 + app 2 replica 앞단 라운드로빈 로드밸런싱 데모
- **Dockerfile(멀티스테이지) + docker-compose.yml**(app×2 + postgres + redis + nats + nginx) — `docker-compose up` 한 번으로 전체 인프라 로컬 실행
- **컨테이너 메모리 제한** — `docker-compose.yml`에서 서비스별 `mem_limit`(또는 `deploy.resources.limits.memory`)로 최대 사용 메모리 상한 지정(예: DB(PostgreSQL) 512MB, Redis 256MB, NATS 128MB 등)

## ERD

To-Be 스키마는 [`docs/ERD.md`](./docs/ERD.md) 참고.

## 레거시 대비 개선점

_작성 예정 (Phase 8) — 하드코딩 자격증명, 평문 비밀번호 비교, 미해결 성능 이슈, 트랜잭션 불일치 등 실제 문제와 해결 방법을 before/after 표로 정리 예정_

- 도서 카탈로그 검색 성능(인덱스 추가 전/후 `EXPLAIN ANALYZE` 실측): [`docs/PERFORMANCE.md`](./docs/PERFORMANCE.md)

## 실행 방법

```bash
docker compose build
docker compose up -d
```

앱(2 replica) + Postgres(Flyway) + Redis + NATS JetStream + Nginx(리버스 프록시/로드밸런서)가 함께 뜬다. `http://localhost:8080/swagger-ui/index.html`에서 Swagger UI를 확인할 수 있다.

각 구성 요소를 왜 이렇게 만들었는지(멀티스테이지 Dockerfile, healthcheck, mem_limit, Nginx 라운드로빈, Flyway 베이스라인 등)와 단계별 명령어·트러블슈팅은 [`docs/DOCKER.md`](./docs/DOCKER.md) 참고.

## Out of Scope

이번 리팩토링은 핵심 대표 모듈(인증, 도서 카탈로그, 장바구니/구매/재고, 리뷰) 4개만 깊게 개선한다.<br> 아래 모듈은 `legacy/DEV24Test`에 그대로 남아있고 신규 스택으로 포팅하지 않는다.

| 모듈 | 제외 이유 | 적용 가능한 패턴 |
|---|---|---|
| 자유게시판(freeboard/freecmt) | 시간 관계상 범위 제외 | 리뷰 모듈과 동일한 페이징/XSS 방지 패턴 적용 가능 |
| 공지사항/이벤트(ne/necmt) | 시간 관계상 범위 제외 | 리뷰 모듈과 동일한 페이징 패턴 적용 가능 |
| FAQ | 단순 CRUD, 소구점 낮음 | 도서 카탈로그 모듈과 동일한 조회/캐싱 패턴 적용 가능 |
| QnA | 시간 관계상 범위 제외 | 리뷰 모듈과 동일한 소유자 검증/페이징 패턴 적용 가능 |
| 마이페이지(주문/환불 내역) | 장바구니/구매 모듈과 중복되는 소구점 | 구매 모듈의 `PurchaseItem` 조회 패턴 재사용 가능 |
| 환불(refund) | 시간 관계상 범위 제외 | 구매 모듈과 동일한 트랜잭션 경계 설계 적용 가능 |

## 레거시 원본

`legacy/DEV24Test`에 원본 프로젝트를 그대로 보존했다(Before 근거).

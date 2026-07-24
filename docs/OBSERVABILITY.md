# 운영 가시성 가이드 (Actuator + Micrometer + Prometheus)

> **핵심 한 줄**: Actuator는 있었지만 `health`만 열려 있었고 값을 저장·조회할 registry도 없었다 — Micrometer(Prometheus 레지스트리)를 추가하고 Prometheus 컨테이너로 실제 스크래핑까지 검증했다. Grafana(시각화)는 이번 범위에 넣지 않았다.

## 목차

1. [왜 지금까지는 `health`만 보였는가](#1-왜-지금까지는-health만-보였는가)
2. [이중 차단 확장 - nginx는 그대로, Spring allowlist만 넓혔다](#2-이중-차단-확장---nginx는-그대로-spring-allowlist만-넓혔다)
3. [Prometheus가 2개 app 인스턴스를 모두 스크래핑하는 원리](#3-prometheus가-2개-app-인스턴스를-모두-스크래핑하는-원리)
4. [커스텀 메트릭 - 이벤트 소비 성공/실패](#4-커스텀-메트릭---이벤트-소비-성공실패)
5. [캐시 히트/미스 메트릭 - 자동 계측이지만 통계 수집 스위치는 따로 켜야 한다](#5-캐시-히트미스-메트릭---자동-계측이지만-통계-수집-스위치는-따로-켜야-한다)
6. [왜 Grafana는 넣지 않았는가](#6-왜-grafana는-넣지-않았는가)
7. [직접 실행해보기](#7-직접-실행해보기)

## 1. 왜 지금까지는 `health`만 보였는가

Phase 1부터 `spring-boot-starter-actuator`는 의존성에 있었고, Docker healthcheck(`docker-compose.yml`의 `app` 서비스)가 `/actuator/health`를 이미 쓰고 있었다. 하지만 `application.properties`엔 이것만 있었다:

```properties
management.endpoints.web.exposure.include=health
```

`health` 하나만 열어둔 이유는 "컴포넌트 상세 정보를 외부에 노출하지 않는다"는 원칙 때문이었다(`management.endpoint.health.show-details=never`도 같은 목적). 문제는 애초에 `metrics`/`prometheus` 자체를 안 열어놨으니, JVM 메모리·GC·HTTP 요청 수·캐시 히트율 같은 지표를 확인할 방법이 아예 없었다는 것 — Actuator가 내부적으로 계측은 하고 있어도 꺼내 볼 창구가 없었다.

이번 작업은 두 가지를 더한다:
1. `management.endpoints.web.exposure.include=health,metrics,prometheus` — 창구를 연다.
2. `io.micrometer:micrometer-registry-prometheus` 의존성 — Prometheus가 긁어갈 수 있는 텍스트 포맷(`/actuator/prometheus`)으로 지표를 노출하는 레지스트리.

## 2. 이중 차단 확장 - nginx는 그대로, Spring allowlist만 넓혔다

`docs/DOCKER.md` 5절에 이미 정리된 "actuator 이중 차단" 구조를 그대로 유지·확장한다:

| 레이어 | 이번 변경 전 | 이번 변경 후 |
|---|---|---|
| nginx(`docker/nginx/default.conf`) | `/actuator/` 전체 `deny all; return 404;` | **변경 없음** — 외부에서는 여전히 전부 404 |
| Spring `management.endpoints.web.exposure.include` | `health`만 | `health,metrics,prometheus` |
| `SecurityConfig.PERMIT_ALL_PATHS` | `/actuator/health`만 permitAll | `/actuator/**` permitAll |

`SecurityConfig`의 URL 패턴을 `/actuator/**`로 넓혔다고 노출 범위가 넓어지는 게 아니다 — **실제 게이트는 `management.endpoints.web.exposure.include`**다. Boot는 이 allowlist에 없는 엔드포인트는 애초에 매핑조차 하지 않아서(Spring Security 필터를 타기 전에 이미 404), URL 패턴을 permitAll로 열어놔도 노출 안 된 엔드포인트는 어차피 존재하지 않는 경로가 된다. 이번에 URL 패턴을 넓힌 이유는 "진짜 방어선이 어디인지"를 코드에 정직하게 반영하기 위해서다.

외부 요청은 여전히 nginx가 `/actuator/`를 통째로 막으므로, `curl http://localhost:8080/actuator/prometheus`(nginx 경유)는 지금도 404다. Prometheus 컨테이너는 nginx를 거치지 않고 `bookstore-net` 내부망에서 `app:8080`에 직접 붙는다 — Docker 자체 healthcheck(`wget http://localhost:8080/actuator/health`, 컨테이너 안에서 직접 확인)와 정확히 같은 신뢰 경계다.

## 3. Prometheus가 2개 app 인스턴스를 모두 스크래핑하는 원리

`app` 서비스는 `deploy.replicas: 2`로 뜬다. Docker Compose의 임베디드 DNS(`127.0.0.11`)는 `app`이라는 이름을 조회하면 두 replica의 IP를 모두 반환하지만, 스크래퍼가 이름을 **한 번만** resolve해서 캐싱해버리면 그중 하나의 IP만 계속 붙잡고 있게 된다.

이 프로젝트는 이 문제를 nginx에서 이미 한 번 풀었다(`docs/DOCKER.md` 5절) — `resolve` 파라미터로 nginx가 TTL마다 `app`을 재조회하게 만들었다. `docker/prometheus/prometheus.yml`은 같은 문제를 Prometheus 쪽 도구로 푼다:

```yaml
scrape_configs:
  - job_name: bookstore-app
    metrics_path: /actuator/prometheus
    dns_sd_configs:
      - names: ["app"]
        type: A
        port: 8080
        refresh_interval: 15s
```

`static_configs`(고정 타겟 목록) 대신 `dns_sd_configs`(DNS 기반 서비스 디스커버리)를 쓰면 `refresh_interval`마다 주기적으로 `app`을 재조회해서 타겟 목록을 최신 상태로 유지한다. 결과적으로 Prometheus UI의 `/targets` 페이지에는 `app:8080`이 아니라 두 replica 각각의 실제 컨테이너 IP:포트가 별도 타겟으로 나타난다 — nginx가 `resolve`로, Prometheus가 `dns_sd_configs`로, 서로 다른 도구지만 "같은 이름 뒤에 여러 인스턴스가 있고 그게 바뀔 수 있다"는 같은 문제를 같은 방식(주기적 재조회)으로 해결한 것이다.

**실제로 겪고 고친 문제 - NATS를 켠 채로 2 replica를 동시에 올리면 기동에 실패했다.** 이 작업을 실제 `docker compose up`으로 검증하던 중 발견했다: `OrderCompletedEventConsumer`/`LowStockEventConsumer`는 NATS JetStream의 **durable push consumer**로 구독하는데, durable push consumer는 기본적으로 동시에 구독자 하나만 허용한다. `app` replica가 2개 뜨면 둘 다 같은 durable 이름(`order-completed-consumer`/`low-stock-consumer`)으로 구독을 시도하고, 늦게 뜬 쪽이 `[SUB-90012] Consumer is already bound to a subscription` 예외로 기동 자체가 실패했다 - `depends_on: service_healthy`로 묶여 있는 `nginx`/`prometheus`까지 덩달아 못 떴다. Actuator/Micrometer 작업 범위 밖의 별도 문제였지만(Phase 4 NATS 통합 이후 2 replica 조합으로 실제 기동 검증이 된 적이 없었던 것으로 보인다), Prometheus의 2-replica 스크래핑을 검증하려면 어차피 2 replica가 정상 기동해야 해서 그 자리에서 같이 고쳤다.

**해결**: 구독을 큐 그룹(`deliverGroup`)으로 묶었다.

```java
PushSubscribeOptions options = PushSubscribeOptions.builder()
        .durable(DURABLE_NAME)
        .deliverGroup(DURABLE_NAME)
        .build();
subscription = jetStream.subscribe(SUBJECT, DURABLE_NAME, dispatcher, this::handle, false, options);
```

큐 그룹으로 묶으면 같은 durable consumer에 여러 인스턴스(큐 그룹 멤버)가 동시에 바인딩될 수 있고, NATS가 그 멤버들 사이에 메시지를 나눠 전달한다 - 각 메시지는 그룹 내 정확히 한 인스턴스에만 전달되므로(at-least-once는 그대로 유지) 적립금 중복 지급 같은 문제도 생기지 않는다.

**고치면서 한 번 더 겪은 함정**: 코드만 고치고 기존 `docker-compose.yml` 볼륨(`natsdata`) 그대로 재기동했더니 이번엔 다른 에러가 났다 - `[SUB-90016] Existing consumer cannot be modified. Changed fields: [deliverGroup]`. NATS JetStream은 이미 존재하는 durable consumer의 `deliverGroup`처럼 근본적인 설정을 나중에 바꾸는 걸 허용하지 않는다(이전 버전이 `deliverGroup` 없이 컨슈머를 이미 만들어뒀기 때문). 로컬 개발 볼륨이라 데이터 보존이 필요 없어서 `docker compose down` 후 `docker volume rm dev24_natsdata`로 지우고 다시 올려서 해결했다 - 운영 환경이라면 컨슈머를 삭제 후 재생성하는 별도 절차가 필요했을 것이다.

**실제로 2 replica로 재검증한 결과**: `app-1`/`app-2` 둘 다 `Healthy`로 정상 기동했고, Prometheus `/targets`에 두 인스턴스가 별도 타겟으로 모두 `UP`으로 잡혔다. 신규 고객으로 구매를 한 번 더 태워보니 이벤트는 두 인스턴스 중 한쪽(큐 그룹 멤버 하나)에만 전달돼 처리됐고 - `dev24_events_processed_total{event="order-completed",result="success"}`가 정확히 1만 늘고 적립금도 정확히 1건만 반영됐다(양쪽에서 중복 처리됐다면 카운터나 적립금이 2로 튀었을 것이다). nginx 라운드로빈도 두 인스턴스(`172.18.0.7`, `172.18.0.8`)에 정상적으로 번갈아 분산되는 것까지 함께 확인했다.

## 4. 커스텀 메트릭 - 이벤트 소비 성공/실패

Phase 4에서 만든 `OrderCompletedEventConsumer`/`LowStockEventConsumer`(`purchase/event` 패키지)는 NATS 메시지를 받아 `process()`로 처리하고, 성공하면 `message.ack()`, 실패하면 `message.nak()`(재전달 요청)을 호출한다. 지금까지는 이 성공/실패가 로그(`log.error(...)`)로만 남았다 — "지금까지 몇 건 처리됐고 몇 건 실패했는지"를 한눈에 볼 방법이 없었다.

`handle(Message message)`의 ack/nak 분기에 하나의 카운터를 태그로 나눠 붙였다:

```java
message.ack();
meterRegistry.counter("dev24.events.processed", "event", "order-completed", "result", "success").increment();
```
```java
message.nak();
meterRegistry.counter("dev24.events.processed", "event", "order-completed", "result", "failure").increment();
```

`LowStockEventConsumer`는 `event` 태그값만 `"low-stock"`으로 바꿔 동일하게 적용했다. 카운터 이름을 이벤트별로 따로 만들지 않고 이름 하나 + 태그 조합(dimensional metric)으로 설계한 이유는, PromQL 한 쿼리로 두 이벤트를 나란히 비교할 수 있게 하기 위해서다:

```promql
sum(rate(dev24_events_processed_total[1m])) by (event, result)
```

(Prometheus 노출 시 메트릭 이름의 `.`은 `_`로, 카운터에는 `_total` 접미사가 자동으로 붙는다 - Micrometer의 Prometheus 네이밍 컨벤션.)

**실제로 구매 흐름을 태워서 검증**: `docker compose up`으로 띄운 뒤 회원가입 → 로그인 → 장바구니 담기 → `POST /api/purchases`까지 실제 호출하고, NATS 컨슈머가 비동기로 소비할 시간을 두고 Prometheus에 직접 쿼리했다.

```
curl 'http://localhost:9090/api/v1/query?query=dev24_events_processed_total'
# → dev24_events_processed_total{event="order-completed",result="success"} 1
```
같은 시점에 `customer.point`도 결제금액의 1%만큼 실제로 올라가 있는 걸 DB에서 확인했다 - 카운터 값이 로그성 넘버가 아니라 실제 비즈니스 처리 결과와 일치한다는 뜻이다.

비즈니스 로직(`process()`)은 건드리지 않았다 - "Message/ack 관심사는 `handle()`이, 실제 처리는 `process()`가" 담당하는 기존 분리 원칙(`docs/NATS.md` 참고, `process()`가 Mockito 단위 테스트 대상인 이유)을 그대로 유지하고, 카운터 증가는 `handle()`의 ack/nak 바로 옆에 뒀다.

## 5. 캐시 히트/미스 메트릭 - 자동 계측이지만 통계 수집 스위치는 따로 켜야 한다

`BookCacheConfig`(`book/config`)가 `@EnableCaching` + `RedisCacheManagerBuilderCustomizer`로 `bookSearch`/`bookDetail` 캐시를 등록해두면, Spring Boot Actuator는 `RedisCacheMeterBinderProvider`(`spring-boot-actuator`, `@ConditionalOnClass(RedisCache.class)`)를 통해 이 두 캐시를 **자동으로** 계측 대상에 등록한다 - `CacheManager`/`Cache` 빈을 직접 코드로 건드릴 필요가 없다.

**그런데 실제로 붙여보니 등록만 되고 값은 항상 0이었다.** `/actuator/prometheus`에 `cache_gets_total{cache="bookDetail",result="hit"}` 같은 지표 "모양"은 바로 나타나는데, 캐시를 아무리 히트시켜도 값이 계속 0에 머물렀다. 원인은 Spring Data Redis의 `RedisCacheWriter`가 **기본적으로 통계 수집기를 no-op으로 만든다**는 것이다 - `RedisCacheMeterBinderProvider`가 내부적으로 읽는 `RedisCache.getStatistics()`가 애초에 아무것도 세지 않고 있었다. Spring Boot는 이 수집기를 켜는 스위치를 프로퍼티 하나로 노출해뒀다:

```properties
spring.cache.redis.enable-statistics=true
```

(`org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration` 소스를 직접 열어 `if (cacheProperties.getRedis().isEnableStatistics()) { builder.enableStatistics(); }`로 되어 있는 걸 확인했다 - 기본값은 `false`.)

`application.properties`에 이 한 줄을 추가한 뒤 실제 컨테이너에서 `GET /api/books/{id}`를 미스 1번·히트 2번 태우고 Prometheus로 직접 쿼리해 확인했다:

```
cache_gets_total{cache="bookDetail",result="hit"}  2
cache_gets_total{cache="bookDetail",result="miss"} 1
```

정리하면 "완전히 공짜"는 아니고, **바인딩 자체는 코드 없이 자동이지만 통계 수집 스위치는 명시적으로 켜야 값이 실제로 오른다**는 게 정확한 설명이다. 이번에 노출을 열면서 `/actuator/prometheus`에 아래 지표가 (스위치를 켠 뒤로는) 추가 코드 없이 실제 값으로 나타난다:

```
cache_gets_total{cache="bookDetail",cache_manager="cacheManager",result="hit", ...}
cache_gets_total{cache="bookDetail",cache_manager="cacheManager",result="miss", ...}
cache_gets_total{cache="bookSearch",cache_manager="cacheManager",result="hit", ...}
cache_gets_total{cache="bookSearch",cache_manager="cacheManager",result="miss", ...}
```

`docs/CACHING.md` 7절은 curl 응답시간을 직접 재서 캐시 효과(상세조회 ~1.8배, 목록검색 ~5.4배)를 증명했다. 이 표준 지표(`cache_gets_total`)로도 같은 사실 - "캐시가 실제로 히트하고 있다" - 을 응답시간 실측 없이 카운터만으로 확인할 수 있다. 둘은 서로 다른 각도(응답시간 vs 히트율)에서 같은 캐시 효과를 보여준다.

## 6. 왜 Grafana는 넣지 않았는가

Prometheus와 Grafana는 역할이 다르다:
- **Prometheus**: 메트릭을 주기적으로 수집(scrape)하고 시계열로 저장, PromQL로 조회. UI는 최소한의 그래프/테이블만 제공.
- **Grafana**: Prometheus(또는 다른 데이터소스)에 저장된 데이터를 대시보드로 시각화하는 프론트엔드. Prometheus 없이는 보여줄 데이터가 없다.

이번 Phase 9 작업 범위는 "지표를 수집하고 실제로 스크래핑되는 것까지 검증"으로 합의했다 - 수집 계층까지다. 대시보드로 예쁘게 보여주는 시각화 계층(Grafana)은 포함하지 않았다. Prometheus UI(`http://localhost:9090`)만으로도 `/targets`(수집 상태 확인)와 `/graph`(PromQL 쿼리)로 이 문서의 검증 목적은 충분히 달성된다. 대시보드가 실제로 필요해지면 Grafana 컨테이너 하나(`docker-compose.yml`에 서비스 추가 + Prometheus를 데이터소스로 연결)만 얹으면 되는 구조라 후속 확장은 어렵지 않다.

## 7. 직접 실행해보기

```bash
# 1. 전체 스택 기동 (app 2 replica + prometheus 포함)
docker compose up -d

# 2. 외부(nginx 경유)에서는 여전히 차단되는지 회귀 확인 - 404여야 정상
curl -i http://localhost:8080/actuator/prometheus

# 3. 컨테이너 내부에서는 정상 응답하는지 확인
docker exec -it $(docker compose ps -q app | head -n1) wget -qO- http://localhost:8080/actuator/prometheus | head -n 20

# 4. Prometheus UI에서 두 app 인스턴스가 모두 UP인지 확인
#    브라우저로 http://localhost:9090/targets 접속 - bookstore-app job 아래 타겟 2개, 둘 다 State=UP

# 5. Swagger UI(http://localhost:8080/swagger-ui/index.html)에서
#    로그인 -> 장바구니 담기 -> 구매 흐름을 한 번 태운다.

# 6. Prometheus UI(http://localhost:9090/graph)에서 PromQL 쿼리로 값이 오르는지 확인
#    dev24_events_processed_total{event="order-completed",result="success"}
#    cache_gets_total{cache="bookDetail"}
```

**실제로 이 순서대로 검증했다(2026-07-24, Docker Desktop 로컬, app 2 replica 그대로)**: 회원가입 → 로그인 → 장바구니 → 구매까지 실제 API 호출로 흐름을 두 번(고객 2명) 태웠고, 매번 `dev24_events_processed_total{event="order-completed",result="success"}`가 정확히 1씩만 늘고 `customer.point`가 결제금액의 1%만큼 실제로 올라간 것을 DB에서 함께 확인했다(3절에서 설명한 큐 그룹 덕분에 두 인스턴스 중 한쪽에서만 처리되고 중복 지급되지 않았다). `cache_gets_total{cache="bookDetail"}`도 미스·히트를 그대로 반영했고(단, 5절에서 설명한 `spring.cache.redis.enable-statistics=true`를 추가한 뒤에야 값이 실제로 움직였다 - 처음엔 지표만 등록되고 값이 0에 고정되는 걸 이 과정에서 직접 발견했다), nginx도 두 인스턴스에 정상적으로 라운드로빈 분산되는 것까지 확인했다.

PowerShell 환경:

```powershell
curl.exe -i http://localhost:8080/actuator/prometheus
docker exec -it $(docker compose ps -q app | Select-Object -First 1) wget -qO- http://localhost:8080/actuator/prometheus
```

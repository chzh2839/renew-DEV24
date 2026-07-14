# 구매완료 이벤트(NATS JetStream 발행/구독) 가이드

`POST /api/purchases` 구매 확정 후, 적립금 지급/알림처럼 주문 확정의 원자성 대상이 아닌 부가 처리를 `OrderCompletedEvent`로 NATS JetStream에 발행하고 별도 컨슈머가 비동기로 소비하는 구조다.<br>
`com.dev24.bookstore.purchase` 패키지(`event`/`config`)와 `com.dev24.bookstore.auth.domain.Customer`가 핵심이다.

## 한눈에 보는 전체 흐름

```
PurchaseCommandService.purchase()  [@Transactional, 커밋 전]
        │
        ▼
ApplicationEventPublisher.publishEvent(OrderCompletedEvent)   ← 스프링 내부 이벤트만 발행 (아직 NATS로는 안 나감)
        │
        ▼
        (스프링이 트랜잭션 커밋)
        │
        ▼
OrderCompletedEventPublisher.publish()
  @TransactionalEventListener(phase = AFTER_COMMIT)            ← 커밋 성공이 보장된 뒤에만 콜백됨
        │
        ▼
jetStream.publish("orders.completed", JSON)                   ← 이제서야 NATS로 실제 발행
        │
        ▼
NATS JetStream 스트림 "ORDERS" (subjects: orders.>)
        │
        ▼
OrderCompletedEventConsumer  (앱 기동 시 durable push consumer로 미리 구독, 별도 스레드)
        │
        ├─ 성공 → customer.addPoint(totalPrice * 1%) 저장 + 알림 로그 → message.ack()
        └─ 실패 → message.nak() → NATS가 재전달(at-least-once)
```

| 컴포넌트 | 역할 | 파일 |
|---|---|---|
| `OrderCompletedEvent` | NATS로 오가는 페이로드(record) | `purchase/event/OrderCompletedEvent.java` |
| `NatsConfig` | NATS 커넥션 빈 + JetStream 스트림("ORDERS") 준비 | `purchase/config/NatsConfig.java` |
| `OrderCompletedEventPublisher` | 트랜잭션 커밋 후에만 실제 NATS 발행 | `purchase/event/OrderCompletedEventPublisher.java` |
| `OrderCompletedEventConsumer` | 앱 기동 시 구독, 적립금 지급 + 알림(시뮬레이션) | `purchase/event/OrderCompletedEventConsumer.java` |
| `PurchaseCommandService.purchase()` | 커밋 직전 `ApplicationEventPublisher`로 발행 트리거 | `purchase/service/PurchaseCommandService.java` |
| `Customer.point` | 적립금 컬럼(`V7__add_point_to_customer.sql`) | `auth/domain/Customer.java` |

## 왜 트랜잭션 커밋 "이후"에 발행해야 하는가

적립금 지급/알림은 주문 확정의 원자성 대상이 아니다 — 발행이 실패하거나 컨슈머가 잠깐 죽어도 주문 자체는 이미 확정된 것이어야 하므로 최종적 일관성(eventual consistency)으로 충분하다. 반대로 커밋되기 **전에** 발행해버리면, 이후 어떤 이유로 트랜잭션이 롤백될 경우 "주문은 없던 일이 됐는데 적립금/알림 이벤트는 이미 나가버린" 불일치가 생긴다.

문제는 `purchase()` 메서드가 스스로 "나 지금 커밋 성공했다"를 알 방법이 없다는 것(`@Transactional`은 프록시가 메서드 종료 후 알아서 커밋한다). 그래서 표준 스프링 패턴대로 **두 단계**로 나눈다:

1. `purchase()` 안(트랜잭션 안, 커밋 전)에서 `ApplicationEventPublisher.publishEvent(...)`로 **스프링 내부 이벤트**만 발행해둔다.
2. 별도 빈의 `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` 메서드가 **트랜잭션이 실제로 커밋된 뒤에만** 콜백되어, 그제서야 NATS로 진짜 발행한다.

이 방식의 장점: 커밋 실패 시 이벤트 자체가 발행되지 않고, 커밋 성공 시에만 발행된다는 걸 스프링 프레임워크가 보장해줘서 별도의 보상 로직(자체 재시도, outbox 등)이 필요 없다.

## 단계별 순서

### 1. 의존성 추가 (`build.gradle.kts`)
```kotlin
implementation("io.nats:jnats:2.25.3")
```

### 2. 설정 프로퍼티 추가 (`application.properties`)
```properties
app.nats.enabled=false
app.nats.url=nats://localhost:4222
```
`app.nats.enabled`가 왜 필요한지는 아래 "커넥션 실패가 앱 전체를 죽이지 않게" 절 참고.

### 3. 이벤트 페이로드 정의
```java
public record OrderCompletedEvent(Long purchaseId, Long customerId, int totalPrice, LocalDateTime purchasedAt) {}
```
다른 DTO들처럼 record로 정의하면 Jackson이 별도 설정 없이 직렬화/역직렬화한다.

### 4. NATS 연결 + 스트림 준비 (`@Configuration`)
```java
@Configuration
@ConditionalOnProperty(name = "app.nats.enabled", havingValue = "true")
public class NatsConfig {

    private static final String STREAM_NAME = "ORDERS";
    private static final String STREAM_SUBJECTS = "orders.>";

    @Bean(destroyMethod = "close")
    public Connection natsConnection(@Value("${app.nats.url}") String natsUrl) throws IOException, InterruptedException {
        return Nats.connect(natsUrl);
    }

    @Bean
    public JetStream jetStream(Connection natsConnection) throws IOException, JetStreamApiException {
        ensureStreamExists(natsConnection);
        return natsConnection.jetStream();
    }

    private void ensureStreamExists(Connection connection) throws IOException, JetStreamApiException {
        JetStreamManagement jsm = connection.jetStreamManagement();
        try {
            jsm.getStreamInfo(STREAM_NAME);
        } catch (JetStreamApiException e) {
            // getStreamInfo가 예외를 던지면(스트림이 없으면) 생성한다 - 재기동 시 이미 있으면 그대로 둔다(idempotent)
            jsm.addStream(StreamConfiguration.builder()
                    .name(STREAM_NAME)
                    .subjects(STREAM_SUBJECTS)
                    .storageType(StorageType.File)
                    .build());
        }
    }
}
```
JetStream은 메시지를 발행할 subject를 담을 **스트림이 미리 존재**해야 동작한다(일반 NATS pub/sub와 다른 점).<br>
`subjects("orders.>")`처럼 와일드카드로 잡아두면 나중에 `orders.low-stock` 같은 다른 subject도 같은 스트림에 재사용할 수 있다.

### 5. 발행자 — 커밋 후에만 실제 발행
```java
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.nats.enabled", havingValue = "true")
public class OrderCompletedEventPublisher {

    private static final String SUBJECT = "orders.completed";
    private final JetStream jetStream;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(OrderCompletedEvent event) {
        try {
            jetStream.publish(SUBJECT, objectMapper.writeValueAsBytes(event));
        } catch (Exception e) {
            // 주문은 이미 커밋되어 있으므로 여기서 실패해도 주문을 되돌리지 않는다 - 로그만 남기고 삼킨다.
            // outbox 패턴 등으로 완전히 보장하려면 별도 설계가 더 필요하다(이 프로젝트 범위 밖).
            log.error("OrderCompletedEvent NATS 발행 실패, purchaseId={}", event.purchaseId(), e);
        }
    }
}
```
`ObjectMapper`는 커스텀 빈을 새로 만들 필요 없이 Spring Boot가 기본 제공하는 빈(JavaTimeModule 자동 등록됨)을 그대로 주입받아 쓰면 된다.

### 6. 실제 트랜잭션 메서드에서 발행 트리거
```java
@Transactional
public PurchaseResponse purchase(String loginId, PurchaseRequest request) {
    // ... 주문 생성/재고 차감/장바구니 삭제 ...

    applicationEventPublisher.publishEvent(
            new OrderCompletedEvent(purchase.getId(), customer.getId(), totalPrice, purchase.getPurchasedAt()));

    return PurchaseResponse.from(purchase);
}
```
`ApplicationEventPublisher`는 스프링이 기본 제공하는 빈이라 그냥 생성자 주입만 받으면 된다. 발행 위치는 메서드의 어디든 상관없다(어차피 실제 NATS 발행은 커밋 후로 미뤄지므로) — 다만 가독성을 위해 트랜잭션의 마지막 처리 단계 근처에 두는 게 자연스럽다.

### 7. 컨슈머 — 별도 구독 + 처리
```java
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.nats.enabled", havingValue = "true")
public class OrderCompletedEventConsumer {

    private static final String SUBJECT = "orders.completed";
    private static final String DURABLE_NAME = "order-completed-consumer";
    private static final int POINT_RATE_PERCENT = 1;

    private final Connection natsConnection;
    private final JetStream jetStream;
    private final CustomerRepository customerRepository;
    private final ObjectMapper objectMapper;
    private Dispatcher dispatcher;

    @PostConstruct
    public void subscribe() throws IOException, JetStreamApiException {
        dispatcher = natsConnection.createDispatcher();
        PushSubscribeOptions options = PushSubscribeOptions.builder().durable(DURABLE_NAME).build();
        // autoAck=false - 처리 성공/실패에 따라 직접 ack/nak하기 위함
        jetStream.subscribe(SUBJECT, dispatcher, this::handle, false, options);
    }

    private void handle(Message message) {
        try {
            OrderCompletedEvent event = objectMapper.readValue(message.getData(), OrderCompletedEvent.class);
            process(event);
            message.ack();   // 정상 처리 완료를 NATS 서버에 알림
        } catch (Exception e) {
            log.error("OrderCompletedEvent 처리 실패 - NATS 재전달 대기", e);
            message.nak();   // 처리 실패, 재전송(redelivery) 요청
        }
    }

    // 단위 테스트 대상 - 실제 비즈니스 처리(적립금 지급 + 알림)만 담당, Message/ack 관심사는 handle()이 처리
    void process(OrderCompletedEvent event) {
        Customer customer = customerRepository.findById(event.customerId())
                .orElseThrow(() -> new IllegalStateException("적립금 지급 대상 고객을 찾을 수 없습니다: " + event.customerId()));
        customer.addPoint(event.totalPrice() * POINT_RATE_PERCENT / 100);
        customerRepository.save(customer);   // 리포지토리 메서드 자체가 개별 트랜잭션이라 별도 @Transactional 불필요

        log.info("[알림] 고객 {}에게 주문 #{} 완료 알림 발송(시뮬레이션)", event.customerId(), event.purchaseId());
    }

    @PreDestroy
    public void shutdown() {
        if (dispatcher != null) {
            natsConnection.closeDispatcher(dispatcher);
        }
    }
}
```
설계 포인트:
- **durable push consumer**:<br>
  - `PushSubscribeOptions.builder().durable(이름)`로 구독하면, 앱이 재기동돼도 NATS 서버가 "어디까지 소비했는지"를 기억해서 못 받은 메시지부터 이어서 준다.
- **manual ack(`autoAck=false`)**:<br>
  - 처리에 성공했을 때만 `ack()`를 호출하고, 실패하면 `nak()`으로 재전달을 요청한다 — 자체 재시도 루프를 짤 필요 없이 JetStream의 at-least-once 전달에 그대로 위임.
- **비즈니스 로직은 `process()`로 분리**:<br>
  - `Message`/`ack` 관심사(`handle()`)와 실제 처리(`process()`)를 나눠야 `process(OrderCompletedEvent)`를 Mockito로 바로 단위 테스트할 수 있다.
- **셀프 호출(self-invocation) 문제 없음**:<br>
  - `@Transactional`은 스프링이 클래스를 감싸 만든 **프록시**가 대신 동작시켜주는 기능이다. 그런데 `this::handle`처럼 이 클래스가 **자기 메서드를 직접 참조**해서 NATS에 콜백으로 넘기면, 그 호출은 프록시를 거치지 않고 원본 객체로 바로 들어온다 — 그래서 `handle()`이나 `process()`에 `@Transactional`을 붙여봤자 조용히 무시된다(둘 다 이 클래스 "안"에서 일어나는 호출이라 마찬가지).
  - 그래서 이 클래스는 애초에 `@Transactional`을 안 쓴다. 대신 `customerRepository.save(customer)`를 호출하는데, `customerRepository`는 별도로 주입받은 **다른 빈**이라 정상적으로 프록시를 거치고, Spring Data JPA가 만들어주는 그 구현체(`SimpleJpaRepository`) 자체에 이미 `@Transactional`이 걸려 있다. 그래서 `save()` 호출 하나로 트랜잭션 문제가 자연스럽게 해결된다.

## 커넥션 실패가 앱 전체를 죽이지 않게 (`app.nats.enabled`)

`Nats.connect(url)`은 빈이 만들어지는 즉시 연결을 시도하고, 실패하면 예외를 던져 **빈 생성 자체가 실패**한다(Redis처럼 최초 사용 시점까지 연결을 미루는 지연 연결 모드가 없음).<br>
이걸 그냥 뒀더니 NATS를 안 띄우는 다른 테스트(`AuthIntegrationTest`, `BookQueryServiceCacheTest` 등 `@SpringBootTest` 전체 컨텍스트를 띄우는 테스트들)까지 전부 컨텍스트 로딩 실패로 깨졌다.

그래서 이미 있던 `app.book-seed.enabled`(카카오 API 시딩 켜고 끄는 플래그)와 동일한 패턴으로, `NatsConfig`/`OrderCompletedEventPublisher`/`OrderCompletedEventConsumer` 세 클래스 모두에 다음을 붙였다:
```java
@ConditionalOnProperty(name = "app.nats.enabled", havingValue = "true")
```
- 기본값(`application.properties`)은 `false` → NATS를 안 쓰는 로컬 실행/테스트는 이 빈들이 아예 생성되지 않아 연결 시도 자체가 없다.
- `docker-compose.yml`의 `app` 서비스 환경변수로 `true`로 켠다(아래 참고).
- NATS를 실제로 검증하는 테스트(`OrderCompletedEventIntegrationTest`)는 `@DynamicPropertySource`로 `true`를 명시적으로 설정한다.

> 참고: `@ConditionalOnProperty`는 프로퍼티가 아예 없어도 기본값이 `matchIfMissing=false`라 `application.properties`에 `app.nats.enabled=false`를 안 적어도 동작은 같다. 그런데도 명시하는 이유는 순전히 가시성(discoverability) 때문 — 이 플래그가 존재하고 기본이 꺼져 있다는 걸 파일만 보고 알 수 있게.

## docker-compose 설정

`docker-compose.yml`의 `nats` 서비스는 JetStream을 활성화해서 띄운다:
```yaml
nats:
  image: nats:2.10-alpine
  command: ["-js", "-sd", "/data", "-m", "8222"]
  ports:
    - "4222:4222"   # NATS client (pub/sub)
    - "8222:8222"   # HTTP 모니터링
```
`app` 서비스 쪽 환경변수:
```yaml
APP_NATS_ENABLED: "true"
APP_NATS_URL: nats://nats:4222
```
`docker-compose up`만 하면 NATS가 뜨고, 앱도 자동으로 발행/구독을 시작한다.

로컬에서 docker-compose 없이 직접 확인하고 싶으면:
```bash
docker run -p 4222:4222 nats:2.10-alpine -js
./gradlew bootRun --args='--app.nats.enabled=true'
```

## 테스트

- **단위 테스트**(`OrderCompletedEventConsumerTest`, Mockito):<br>
  - `process(event)`를 직접 호출해 `customer.point`가 정확히 늘고 `customerRepository.save()`가 호출되는지 검증. NATS/JSON 관심사는 손대지 않는다.
- **통합 테스트**(`OrderCompletedEventIntegrationTest`, Testcontainers Postgres+NATS):<br>
  - 실제 `purchaseCommandService.purchase(...)` 호출 → 커밋 후 발행 → 이미 구독 중인 컨슈머가 소비하는 전체 흐름을 검증한다. NATS 컨테이너는 `GenericContainer<>("nats:2.10-alpine").withCommand("-js")`로 띄우고, `@DynamicPropertySource`로 `app.nats.enabled=true`/`app.nats.url`을 주입한다.
  - 소비는 별도 스레드에서 비동기로 일어나므로 즉시 단언할 수 없어 **Awaitility**(`spring-boot-starter-test`에 기본 포함, 별도 의존성 추가 불필요)로 폴링한다:
```java
await().atMost(Duration.ofSeconds(10))
        .untilAsserted(() -> assertThat(customerRepository.findById(customerId).orElseThrow().getPoint())
                .isEqualTo(예상적립금));
```

## 지금 포함되지 않은 것

- 실제 알림 채널(이메일/SMS/푸시) 연동 — 로그로 시뮬레이션만 한다.
- 적립금 정책 고도화(등급별 차등 등) — 결제금액의 1% 고정 예시.
- Outbox 패턴 등 "DB 커밋"과 "NATS 발행" 사이의 완전한 원자성 보장 — 발행 실패는 로그만 남기고 유실을 감수한다.
- `LowStockEvent` 발행 — 이 문서의 패턴(스트림 subject `orders.>` 재사용)을 그대로 적용하면 된다.
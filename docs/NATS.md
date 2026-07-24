# 구매완료/재고부족 이벤트(NATS JetStream 발행/구독) 가이드

`POST /api/purchases` 구매 확정 후, 적립금 지급/알림처럼 주문 확정의 원자성 대상이 아닌 부가 처리를 NATS JetStream에 발행하고 별도 컨슈머가 비동기로 소비하는 구조다. 같은 발행/구독 패턴이 두 가지 시나리오에 재사용된다:
- **`OrderCompletedEvent`**: 구매 완료 → 적립금 지급 + 알림
- **`LowStockEvent`**: 재고가 안전재고 이하로 처음 떨어지는 순간 → 관리자에게 재입고 알림

`com.dev24.bookstore.purchase` 패키지(`event`/`config`)와 `com.dev24.bookstore.auth.domain.Customer`/`Admin`이 핵심이다.

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
| `OrderCompletedEventConsumer` | 앱 기동 시 구독, 적립금 지급 + 고객 이메일 알림 | `purchase/event/OrderCompletedEventConsumer.java` |
| `PurchaseCommandService.purchase()` | 커밋 직전 `ApplicationEventPublisher`로 발행 트리거 | `purchase/service/PurchaseCommandService.java` |
| `Customer.point` | 적립금 컬럼(`V2__create_customer_table.sql`) | `auth/domain/Customer.java` |
| `LowStockEvent` | 안전재고 이하 도달 시 오가는 페이로드(record) | `purchase/event/LowStockEvent.java` |
| `LowStockEventPublisher` | 트랜잭션 커밋 후에만 실제 NATS 발행(subject `orders.low-stock`) | `purchase/event/LowStockEventPublisher.java` |
| `LowStockEventConsumer` | 앱 기동 시 구독, 재고 관리자(STOCK_ADMIN) 전원에게 이메일 알림 | `purchase/event/LowStockEventConsumer.java` |
| `EmailNotificationSender` | 두 컨슈머가 공유하는 이메일 발송 창구(발송 실패는 로그만 남기고 삼킴) | `common/notification/EmailNotificationSender.java` |

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
> 위 코드는 처음 이 패턴을 설명할 때 쓴 기본형이다. 실제 코드에서는 `log.info(...)` 알림 줄이 진짜 이메일 발송(`EmailNotificationSender`)으로 바뀌어 있다 — 자세한 내용은 아래 "실제 알림 채널 연동 — 이메일" 절 참고.

설계 포인트:
- **durable push consumer + 큐 그룹(`deliverGroup`)**:<br>
  - `PushSubscribeOptions.builder().durable(이름)`로 구독하면, 앱이 재기동돼도 NATS 서버가 "어디까지 소비했는지"를 기억해서 못 받은 메시지부터 이어서 준다.
  - `app`이 2 replica로 뜨는 이 프로젝트 구조상, 큐 그룹 없이 같은 durable 이름을 두 인스턴스가 동시에 구독하면 durable push consumer는 구독자를 하나만 허용하기 때문에 두 번째 인스턴스가 `[SUB-90012] Consumer is already bound to a subscription`로 기동 자체에 실패한다(실제로 겪음, `docs/OBSERVABILITY.md` 3절 참고). 그래서 `.deliverGroup(DURABLE_NAME)`을 함께 지정하고 `jetStream.subscribe(SUBJECT, DURABLE_NAME, dispatcher, this::handle, false, options)`처럼 큐 이름을 넘겨 큐 그룹으로 구독한다 - 같은 durable에 여러 인스턴스가 동시에 바인딩되고, NATS가 그 멤버들 사이에 메시지를 나눠준다(각 메시지는 그룹 내 한 인스턴스에만 전달돼 적립금 중복 지급 같은 문제가 생기지 않는다).
- **manual ack(`autoAck=false`)**:<br>
  - 처리에 성공했을 때만 `ack()`를 호출하고, 실패하면 `nak()`으로 재전달을 요청한다 — 자체 재시도 루프를 짤 필요 없이 JetStream의 at-least-once 전달에 그대로 위임.
- **비즈니스 로직은 `process()`로 분리**:<br>
  - `Message`/`ack` 관심사(`handle()`)와 실제 처리(`process()`)를 나눠야 `process(OrderCompletedEvent)`를 Mockito로 바로 단위 테스트할 수 있다.
- **셀프 호출(self-invocation) 문제 없음**:<br>
  - `@Transactional`은 스프링이 클래스를 감싸 만든 **프록시**가 대신 동작시켜주는 기능이다. 그런데 `this::handle`처럼 이 클래스가 **자기 메서드를 직접 참조**해서 NATS에 콜백으로 넘기면, 그 호출은 프록시를 거치지 않고 원본 객체로 바로 들어온다 — 그래서 `handle()`이나 `process()`에 `@Transactional`을 붙여봤자 조용히 무시된다(둘 다 이 클래스 "안"에서 일어나는 호출이라 마찬가지).
  - 그래서 이 클래스는 애초에 `@Transactional`을 안 쓴다. 대신 `customerRepository.save(customer)`를 호출하는데, `customerRepository`는 별도로 주입받은 **다른 빈**이라 정상적으로 프록시를 거치고, Spring Data JPA가 만들어주는 그 구현체(`SimpleJpaRepository`) 자체에 이미 `@Transactional`이 걸려 있다. 그래서 `save()` 호출 하나로 트랜잭션 문제가 자연스럽게 해결된다.

## `LowStockEvent` — 같은 패턴을 두 번째 시나리오에 재사용

`LowStockEvent`는 위 1~7단계와 완전히 동일한 구조(record 페이로드 → 커밋 후 발행 → durable consumer 구독)를 그대로 따른다. 다른 점은 딱 두 가지뿐이다.

**1. subject만 다르고 스트림은 그대로 재사용한다** — `NatsConfig`의 스트림이 이미 `subjects("orders.>")`로 잡혀 있어서, `orders.low-stock`이라는 새 subject를 받기 위해 `NatsConfig`를 손댈 필요가 없다. 발행자(`LowStockEventPublisher`)/컨슈머(`LowStockEventConsumer`)만 `OrderCompletedEventPublisher`/`OrderCompletedEventConsumer`를 그대로 복사해 subject와 페이로드 타입만 바꾸면 된다.

**2. 발행 조건이 "상태"가 아니라 "전이(transition)"다** — 단순히 "지금 재고가 안전재고 이하다"가 아니라 "방금 그 경계를 처음 넘었다"일 때만 발행해야 한다. `PurchaseCommandService.purchase()`에서 차감 전/후 수량을 비교한다:
```java
int quantityBeforeDecrease = stock.getQuantity();
stock.decreaseQuantity(cartItem.getQuantity());

// 안전재고 이하로 "떨어지는 순간"에만 발행 - 이미 안전재고 이하인 상태에서 추가 구매가 계속 들어와도
// 재발행하지 않아 관리자에게 같은 알림이 반복되는 걸 막는다.
if (quantityBeforeDecrease > stock.getSafetyStock() && stock.getQuantity() <= stock.getSafetyStock()) {
    applicationEventPublisher.publishEvent(new LowStockEvent(
            stock.getBook().getId(), stock.getQuantity(), stock.getSafetyStock(), LocalDateTime.now()));
}
```
`beforeQuantity > safetyStock`(전에는 여유 있었음)이면서 `afterQuantity <= safetyStock`(지금은 임계치 이하)일 때만 참이 된다 — 이미 임계치 이하인 상태에서 또 구매가 들어와도(`beforeQuantity`가 이미 `safetyStock` 이하) 조건의 앞부분이 거짓이라 재발행되지 않는다.

실제로는 이 케이스가 발생하기도 쉽지 않다:<br>
재고 검증 로직(`stock.getQuantity() - stock.getSafetyStock() < cartItem.getQuantity()`) 자체가 "구매 가능 수량 = 재고 - 안전재고"를 강제하기 때문에, 재고가 이미 안전재고 이하로 내려간 시점부터는 구매 가능 수량이 0 이하가 되어 그 이후의 구매 시도는 전부 `INSUFFICIENT_STOCK`으로 막힌다.<br>
즉 이 전이는 한 `Stock`당 사실상 한 번만 일어난다(관리자가 재입고해서 다시 안전재고 위로 올라간 뒤 또 내려가는 경우는 예외).

**컨슈머 쪽 차이**:<br>
`LowStockEventConsumer.process()`는 `CustomerRepository` 대신 `AdminRepository`로 알림 대상을 조회한다 — 다만 재고를 등록한 특정 관리자 1명이 아니라 **역할이 재고 관리자(`AdminRole.STOCK_ADMIN`)인 관리자 전원**에게 보낸다(`LowStockEvent`엔 이제 특정 관리자 id가 없다).<br>
`email_notification_history`로 "실제로 메일이 갔는지"를 코드로 검증한다.<br>
`LowStockEventIntegrationTest`, `OrderCompletedEventIntegrationTest` — 동일한 Postgres+NATS Testcontainers 골격에 Mailpit 컨테이너를 더해, 구매로 안전재고 임계치를 넘기면 실제 NATS 발행 → 컨슈머 소비 → 실제 SMTP 발송까지 이어져 `email_notification_history`에 `SUCCESS`가 기록되고 Mailpit에도 실제로 도착하는지(REST API로 재확인) 끝까지 검증한다.

## 실제 알림 채널 연동 — 이메일(`EmailNotificationSender`)

두 컨슈머의 "알림"은 로그 시뮬레이션이 아니라 실제 이메일(SMTP)로 나간다. 수신자는 이벤트마다 다르다:
- `OrderCompletedEvent` → 그 주문을 한 `Customer.email`
- `LowStockEvent` → `AdminRepository.findAllByAdminRole(AdminRole.STOCK_ADMIN)`으로 찾은 관리자 전원의 `email`

### 발송을 한 곳에 모은다
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationSender {
    private final JavaMailSender mailSender;

    public void send(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            log.warn("이메일 주소가 없어 알림을 보내지 않습니다. subject={}", subject);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("이메일 발송 실패, to={}, subject={}", to, subject, e);
        }
    }
}
```
**왜 실패를 여기서 삼키는가(중요)**:<br>
`OrderCompletedEventConsumer.process()`는 이미 `customer.addPoint()` + `save()`로 적립금을 커밋한 "뒤"에 이 메서드를 호출한다. 메일 발송 예외가 `handle()`까지 전파되면 `nak()` → NATS 재전달 → `process()` 재실행 → **적립금 중복 지급**(멱등성 키 없음)으로 이어진다. 그래서 알림 발송 실패는 핵심 처리(적립금 지급, 재고관리자 조회)와 완전히 분리해 로그만 남기고 삼킨다 — `OrderCompletedEventPublisher`의 NATS 발행 실패 처리와 같은 원칙.

### `Admin`에 `email` + `adminRole` 추가 (이름 충돌 주의)
이 코드베이스엔 이미 `com.dev24.bookstore.auth.domain.Role`(`CUSTOMER`/`ADMIN`)이 있고, 이건 **Spring Security 인가 역할**이다(JWT 클레임, `@PreAuthorize("hasRole('ADMIN')")`에 쓰임 — `Admin.getRole()`은 지금도 항상 `Role.ADMIN`을 반환). "재고 관리자"는 알림 대상 그룹 분류일 뿐 인가와 무관한 **완전히 다른 개념**이라, 같은 `Role` enum에 얹지 않고 별도의 `AdminRole`(`GENERAL`/`STOCK_ADMIN`) enum을 새로 만들어 분리했다. 인증/인가 코드는 전혀 건드리지 않는다.

`Admin`은 가입 API 없이 직접 생성되어 왔고 기존 3-인자 생성자(`loginId, passwordHash, name`) 호출부가 여러 테스트에 있어서, 그 생성자는 그대로 두고(`email=null`, `adminRole=GENERAL`로 채움) `email`/`adminRole`까지 지정하는 5-인자 생성자를 추가로 뒀다.

### NATS 때와 달리 별도 on/off 플래그가 필요 없는 이유
`JavaMailSenderImpl`은 필드만 설정하는 순수 POJO라 빈 생성 시점에 네트워크 연결을 시도하지 않는다(`Nats.connect()`처럼 즉시 접속 후 실패하는 구조가 아님) — `send()`를 실제로 호출할 때만 SMTP에 접속한다. 메일을 실제로 보내는 코드는 두 컨슈머 안에만 있고, 이미 `app.nats.enabled`로 게이팅되어 있어(NATS 없이는 이 컨슈머들 자체가 안 만들어짐) 다른 테스트/로컬 실행에 영향을 주지 않는다.

### 메일 서버 — Mailpit
`docker-compose.yml`에 `mailpit`(image `axllent/mailpit`) 서비스를 추가했다: SMTP 1025, 웹 UI/REST API 8025. `app` 서비스는 `SPRING_MAIL_HOST=mailpit`, `SPRING_MAIL_PORT=1025`로 연결한다. `docker compose up -d` 후 `http://localhost:8025`에서 실제로 도착한 메일을 확인할 수 있다. 로컬에서 docker-compose 없이 확인하려면:
```bash
docker run -p 1025:1025 -p 8025:8025 axllent/mailpit
```

### 실제로 메일이 발송되는지 확인하는 방법

**`./gradlew test`만 돌려서는 실제 메일이 발송되지 않는다** — 착각하기 쉬운 부분이라 명확히 짚어둔다.
- `EmailNotificationSenderTest`/`OrderCompletedEventConsumerTest`/`LowStockEventConsumerTest`는 전부 `JavaMailSender`/`EmailNotificationSender` 자체를 Mockito로 mock 처리한다 — 실제 SMTP 연결이 아예 일어나지 않는다.
- `OrderCompletedEventIntegrationTest`는 mock 없이 진짜 `EmailNotificationSender` → `JavaMailSender` 코드 경로를 타지만, 이 테스트는 Testcontainers로 Postgres+NATS만 띄우고 Mailpit은 띄우지 않는다. 그래서 `spring.mail.host=localhost:1025`로 접속을 시도해도 받아줄 서버가 없어 연결이 실패하는데, `EmailNotificationSender.send()`가 실패를 로그만 남기고 삼키도록 설계되어 있어(적립금 중복 지급 방지, 위 설명 참고) 테스트 자체는 그냥 통과해버린다 — 메일은 조용히 발송 실패한다.

**실제 발송을 눈으로 확인하려면**:
1. `docker compose up -d`로 전체 스택(Mailpit 포함)을 띄운다.
2. Swagger UI(`http://localhost:8080/swagger-ui/index.html`) 등으로 실제 회원가입 → 로그인 → 장바구니 → 구매 흐름을 태운다(구매 완료 시 `OrderCompletedEvent`가 발행되고, 재고가 안전재고 이하로 떨어지면 `LowStockEvent`도 함께 발행된다).
3. `http://localhost:8025`(Mailpit 웹 UI)를 열어 실제로 도착한 메일을 확인한다.

**자동화된 테스트로 검증하고 싶다면**(이번 범위엔 없음, 필요 시 추가 가능):<br>
`OrderCompletedEventIntegrationTest`처럼 `GenericContainer<>("axllent/mailpit")`를 Testcontainers로 띄우고 `@DynamicPropertySource`로 `spring.mail.host`/`port`를 그 컨테이너로 연결한 뒤, Mailpit의 REST API(`GET http://<mailpit>:8025/api/v1/messages`)를 호출해 실제로 메일이 도착했는지 Awaitility로 폴링하면 된다.

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

### IDE에서 직접 실행하는 경우 (docker-compose는 인프라만 띄운 상태)

`docker compose up -d`로 postgres/redis/nats/mailpit **인프라 컨테이너만** 띄우고 앱 자체는 IDE의 Run/Debug Configuration으로 실행하는 경우,<br>
`docker-compose.yml`의 `APP_NATS_ENABLED=true`는 `app` 컨테이너에만 주입되는 환경변수라 적용되지 않는다 — `application.properties`의 기본값 `app.nats.enabled=false`가 그대로 쓰여서 `NatsConfig`/`OrderCompletedEventPublisher`/컨슈머 빈들이 아예 생성되지 않는다. 구매 자체(재고 차감, 주문 생성)는 NATS와 무관하게 정상 커밋되므로 겉으로는 성공한 것처럼 보이지만, `applicationEventPublisher.publishEvent(OrderCompletedEvent)`를 받아줄 리스너가 없어 NATS 발행 → 컨슈머 소비 → 이메일 발송(`EmailNotificationSender`) → `email_notification_history` 저장까지 이어지는 흐름 전체가 조용히 아무 일도 안 일어난다.

이 경우 `application-local.properties`(`app.nats.enabled=true`, `app.nats.url=nats://localhost:4222` — 인프라 컨테이너들이 `localhost`에 포트 매핑되어 있으므로 기존 기본값과 동일한 URL)를 Run/Debug Configuration의 **Active profiles**에 `local`을 추가해 적용하거나, Program arguments에 `--app.nats.enabled=true`를 직접 넘기면 된다.

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
- 관리자가 실제로 재고를 다시 채우는 재입고 기능 자체 — `LowStockEvent`는 "알림"까지만 담당한다.
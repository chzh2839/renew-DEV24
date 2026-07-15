# 구매 플로우 `@Transactional` 경계 가이드

`POST /api/purchases`(고객 전용, `PurchaseController` → `PurchaseCommandService.purchase(...)`)가 장바구니 선택 항목을 실제 주문으로 확정하는 흐름이다. `com.dev24.bookstore.purchase` 패키지(`domain`/`repository`/`service`/`controller`)가 핵심이다.

## 트랜잭션 경계: 어디서 시작해서 어디서 끝나는가

`PurchaseCommandService.purchase()` 메서드 전체가 `@Transactional` 하나로 묶여 있다:

```
[시작] 고객 조회(loginId) → 장바구니 항목 조회/소유권 검증
        → Purchase(주문 헤더) 저장
        → for 각 장바구니 항목:
             Stock 조회 → 재고 충분? → Stock 차감 → PurchaseItem(주문 라인) 저장
        → 장바구니 항목 삭제
[끝, 커밋]
```

이 중 하나라도 `BusinessException`을 던지면(재고 부족, 존재하지 않는/타인 소유 장바구니 항목 등) 스프링이 트랜잭션을 롤백한다 — 이미 차감된 재고, 이미 저장된 `Purchase`/`PurchaseItem` 행, 이미 삭제 시도된 장바구니까지 전부 없던 일이 된다. `PurchaseCommandServiceTransactionalTest`(Testcontainers, 실제 Postgres)가 이걸 직접 증명한다: 장바구니 2건 중 두 번째 항목에서 재고 부족으로 실패시키면, 이미 재고가 차감됐어야 할 첫 번째 항목의 `Stock.quantity`도 원래 값 그대로 남아있는지 확인한다.

## 왜 재고 차감이 트랜잭션 "안"에 있어야 하는가

재고 차감과 주문 생성이 별도 트랜잭션이면, 재고는 차감됐는데 주문 생성이 실패하는 상황(또는 그 반대)이 가능해진다 — 재고만 사라지고 주문 기록은 없는 데이터 불일치(오버셀의 다른 형태). 하나의 트랜잭션으로 묶으면 "재고 차감"과 "주문 확정"이 원자적으로 함께 성공하거나 함께 실패한다.

## 낙관적 락 충돌 처리 및 안전재고 검증 (완료)

- **`Stock.version`(`@Version`) 충돌 처리**:<br>
  - 동시 수정 시 JPA가 던지는 `ObjectOptimisticLockingFailureException`을 `GlobalExceptionHandler`가 잡아 `ErrorCode.STOCK_CONFLICT`(409, `P002`)로 응답한다. 자동 재시도는 하지 않으며, 재시도 여부는 클라이언트 책임이다.
- **`safetyStock` 기반 판매 가능 수량 검증**:<br>
  - `purchase()`의 체크가 `stock.quantity - stock.safetyStock < 요청수량`으로 동작해, 재고가 안전재고 이하로 내려가는 주문은 `INSUFFICIENT_STOCK`으로 거부된다.
- **동시성 시나리오 테스트**:<br>
  - `PurchaseCommandServiceTransactionalTest#purchase_concurrentRequestsOnSameStock_onlyOneSucceedsAndNeverOversells`가 같은 `Stock`(재고 1개)에 대한 동시 구매 요청 중 하나만 성공하고, 오버셀(음수 재고)이 발생하지 않음을 검증한다.

## 구매완료 이벤트(`OrderCompletedEvent`) NATS 발행/소비 (완료)

- **커밋 이후에만 발행**:<br>
  - `purchase()`는 `ApplicationEventPublisher`로 Spring 애플리케이션 이벤트만 발행해두고(아직 커밋 전),<br>
  별도 빈인 `OrderCompletedEventPublisher`의 `@TransactionalEventListener(phase = AFTER_COMMIT)`가 트랜잭션이 실제로 커밋된 뒤에만 콜백되어 NATS JetStream(subject `orders.completed`)으로 실제 발행한다 — "주문은 롤백됐는데 이벤트는 나간" 상황을 프레임워크 차원에서 차단.
- **별도 컨슈머가 비동기 소비**:<br>
  - `OrderCompletedEventConsumer`가 앱 기동 시 durable push consumer로 구독해두고, 발행자와 스레드/트랜잭션을 공유하지 않고 소비한다.<br>
  - 처리 내용은 적립금 지급(`Customer.point`, 결제금액의 1%)과 `EmailNotificationSender`를 통한 실제 이메일 알림(`Customer.email`로 발송, 로컬/CI는 Mailpit). 실패 시 `message.nak()`으로 JetStream이 재전달하도록 위임.
- **`app.nats.enabled` 플래그**:<br>
  - NATS 연결은 커넥션 생성 즉시 시도되고 실패하면 빈 생성이 실패하므로(Redis처럼 지연 연결이 아님), `app.book-seed.enabled`와 동일한 패턴으로 기본값 `false` 뒤에 숨겨뒀다 — 그래야 NATS를 안 쓰는 나머지 테스트/로컬 실행이 이 실패로 덩달아 죽지 않는다. docker-compose와 NATS 관련 테스트에서만 `true`로 켠다.
- 통합 테스트(`OrderCompletedEventIntegrationTest`, Testcontainers Postgres+NATS)가 실제 `purchase()` 호출부터 컨슈머의 적립금 반영까지 Awaitility로 폴링 검증한다.

## 안전재고 이하 도달 이벤트(`LowStockEvent`) NATS 발행/소비 (완료)

`OrderCompletedEvent`와 완전히 동일한 발행/구독 패턴을 재사용한다(자세한 설명은 `docs/NATS.md` 참고). 다른 점은 두 가지뿐이다.

- **발행 조건은 "상태"가 아니라 "전이(transition)"**:<br>
  - `purchase()`가 재고 차감 전/후 수량을 비교해 `beforeQuantity > safetyStock`이었다가 `afterQuantity <= safetyStock`이 된 경우에만 발행한다. 이미 안전재고 이하인 상태에서 추가 구매가 들어와도 재발행하지 않는다(관리자 알림 스팸 방지) — 사실 재고 검증 로직(`quantity - safetyStock < 요청수량`)이 안전재고 이하에서는 구매 자체를 막아버려서, 한 `Stock`당 이 전이는 사실상 한 번만 일어난다.
- **subject만 다르고 스트림은 재사용**:<br>
  - `NatsConfig`의 스트림이 이미 `subjects("orders.>")`라 `orders.low-stock`을 그대로 받는다 — `NatsConfig` 변경 없음. `LowStockEventConsumer`는 `AdminRepository.findAllByAdminRole(AdminRole.STOCK_ADMIN)`으로 찾은 재고 관리자 **전원**에게 `EmailNotificationSender`로 재입고 알림 이메일을 보낸다(적립금처럼 DB에 남는 변화가 없어 통합 테스트는 두지 않고 단위 테스트만 둔다 — 발행-구독 배관 자체는 `OrderCompletedEventIntegrationTest`가 이미 검증).
- **`Admin.email`/`AdminRole` 신규 추가**:<br>
  - `Role`(`CUSTOMER`/`ADMIN`, Spring Security 인가 역할)과는 완전히 별개인 `AdminRole`(`GENERAL`/`STOCK_ADMIN`, 알림 대상 그룹 분류)을 새로 추가했다 — 자세한 이름 충돌 이유는 `docs/NATS.md` 참고. 인증/인가 코드는 무변경.


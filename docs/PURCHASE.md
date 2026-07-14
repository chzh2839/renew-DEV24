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

## 지금 포함되지 않은 것 (다음 Phase 4 항목)

- **`OrderCompletedEvent`(NATS JetStream)**: 적립금/알림은 이 트랜잭션의 원자성 대상이 아니다 — 커밋 "이후"에 비동기로 발행해야 한다(최종적 일관성으로 충분). `purchase()`가 반환하기 직전, 커밋 성공이 보장된 지점에 발행 코드가 들어갈 자리다.
- **`LowStockEvent`**: 차감 후 `quantity`가 `safetyStock` 이하로 떨어졌는지 확인해 발행.

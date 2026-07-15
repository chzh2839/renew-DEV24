package com.dev24.bookstore.purchase.event;

import java.time.LocalDateTime;

// 구매로 재고가 안전재고(safetyStock) 이하로 "떨어지는 순간"에만 NATS JetStream(subject: orders.low-stock)으로 발행되는 페이로드.
// OrderCompletedEvent와 동일한 발행/구독 패턴을 재사용한다(docs/NATS.md 참고).
public record LowStockEvent(Long bookId, int remainingQuantity, int safetyStock, LocalDateTime occurredAt) {
}

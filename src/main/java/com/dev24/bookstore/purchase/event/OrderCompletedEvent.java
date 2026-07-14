package com.dev24.bookstore.purchase.event;

import java.time.LocalDateTime;

// 구매 확정 트랜잭션 커밋 후 NATS JetStream(subject: orders.completed)으로 발행되는 페이로드.
// 적립금 지급/알림처럼 주문 확정의 원자성 대상이 아닌 부가 처리를 비동기로 분리하기 위함.
public record OrderCompletedEvent(Long purchaseId, Long customerId, int totalPrice, LocalDateTime purchasedAt) {
}

package com.dev24.bookstore.purchase.event;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.nats.client.JetStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// PurchaseCommandService.purchase()가 발행한 Spring 애플리케이션 이벤트를,
// 트랜잭션이 실제로 커밋된 뒤에만 받아 NATS JetStream으로 실제 발행한다.
// OrderCompletedEventPublisher와 동일 패턴 - subject/이벤트 타입만 다르다.
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.nats.enabled", havingValue = "true")
public class LowStockEventPublisher {

    private static final String SUBJECT = "orders.low-stock";

    private final JetStream jetStream;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(LowStockEvent event) {
        try {
            jetStream.publish(SUBJECT, objectMapper.writeValueAsBytes(event));
        } catch (Exception e) {
            // 재고 차감 자체는 이미 커밋되어 있으므로 여기서 실패해도 되돌리지 않는다 - 알림 유실은 감수하는
            // 트레이드오프(OrderCompletedEvent와 동일, docs/NATS.md 참고).
            log.error("LowStockEvent NATS 발행 실패, bookId={}", event.bookId(), e);
        }
    }
}

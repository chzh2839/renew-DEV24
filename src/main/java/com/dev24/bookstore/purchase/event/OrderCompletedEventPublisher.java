package com.dev24.bookstore.purchase.event;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.nats.client.JetStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.nats.enabled", havingValue = "true")
public class OrderCompletedEventPublisher {

    private static final String SUBJECT = "orders.completed";

    private final JetStream jetStream;
    private final ObjectMapper objectMapper;

    // PurchaseCommandService.purchase()가 발행한 Spring 애플리케이션 이벤트를,
    // 트랜잭션이 실제로 커밋된 뒤에만 받아 NATS JetStream으로 실제 발행한다 - 커밋 전에 발행되어 "주문은 롤백됐는데 이벤트는 나간" 상황을 막는다.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(OrderCompletedEvent event) {
        try {
            jetStream.publish(SUBJECT, objectMapper.writeValueAsBytes(event));
        } catch (Exception e) {
            // 주문 자체는 이미 커밋되어 있으므로 여기서 실패해도 주문을 되돌리지 않는다 - 적립금/알림 유실은
            // 감수하는 트레이드오프(README 참고). outbox 패턴 등 완전한 보장은 범위 밖.
            log.error("OrderCompletedEvent NATS 발행 실패, purchaseId={}", event.purchaseId(), e);
        }
    }
}

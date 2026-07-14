package com.dev24.bookstore.purchase.event;

import java.io.IOException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.dev24.bookstore.auth.domain.Admin;
import com.dev24.bookstore.auth.repository.AdminRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;
import io.nats.client.PushSubscribeOptions;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// LowStockEventPublisher가 발행한 메시지를 별도로 구독해 비동기로 소비한다(OrderCompletedEventConsumer와 동일 구조).
// 재고를 등록한 관리자에게 재입고 알림을 보낸다(시뮬레이션). 실패하면 nak()으로 JetStream이 재전달하게 둔다.
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.nats.enabled", havingValue = "true")
public class LowStockEventConsumer {

    private static final String SUBJECT = "orders.low-stock";
    private static final String DURABLE_NAME = "low-stock-consumer";

    private final Connection natsConnection;
    private final JetStream jetStream;
    private final AdminRepository adminRepository;
    private final ObjectMapper objectMapper;

    private Dispatcher dispatcher;
    private JetStreamSubscription subscription;

    @PostConstruct
    public void subscribe() throws IOException, JetStreamApiException {
        dispatcher = natsConnection.createDispatcher();
        PushSubscribeOptions options = PushSubscribeOptions.builder()
                .durable(DURABLE_NAME)
                .build();
        // autoAck=false - 처리 성공/실패에 따라 직접 ack/nak하기 위함
        subscription = jetStream.subscribe(SUBJECT, dispatcher, this::handle, false, options);
    }

    private void handle(Message message) {
        try {
            LowStockEvent event = objectMapper.readValue(message.getData(), LowStockEvent.class);
            process(event);
            message.ack();
        } catch (Exception e) {
            log.error("LowStockEvent 처리 실패 - NATS 재전달 대기", e);
            message.nak();
        }
    }

    // 단위 테스트 대상 - 실제 비즈니스 처리(재입고 알림)만 담당, Message/ack 관심사는 handle()이 처리
    void process(LowStockEvent event) {
        Admin admin = adminRepository.findById(event.adminId())
                .orElseThrow(() -> new IllegalStateException("알림 대상 관리자를 찾을 수 없습니다: " + event.adminId()));

        // 실제 알림 채널(이메일/SMS/푸시) 연동은 범위 밖이라 로그로 시뮬레이션한다.
        log.warn("[재입고 알림] 관리자 {}({})에게 도서 #{} 재고 부족 알림 발송(시뮬레이션) - 남은 수량={}, 안전재고={}",
                admin.getName(), admin.getId(), event.bookId(), event.remainingQuantity(), event.safetyStock());
    }

    @PreDestroy
    public void shutdown() {
        if (dispatcher != null) {
            natsConnection.closeDispatcher(dispatcher);
        }
    }
}

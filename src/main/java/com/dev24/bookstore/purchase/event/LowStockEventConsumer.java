package com.dev24.bookstore.purchase.event;

import java.io.IOException;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.dev24.bookstore.auth.domain.Admin;
import com.dev24.bookstore.auth.domain.AdminRole;
import com.dev24.bookstore.auth.repository.AdminRepository;
import com.dev24.bookstore.common.notification.EmailNotificationSender;
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
// 재고 관리자(AdminRole.STOCK_ADMIN) 전원에게 재입고 알림 이메일을 보낸다. 실패하면 nak()으로 JetStream이 재전달하게 둔다.
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
    private final EmailNotificationSender emailNotificationSender;
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

    // 단위 테스트 대상 - 실제 비즈니스 처리(재고 관리자 전원에게 재입고 알림)만 담당, Message/ack 관심사는 handle()이 처리
    void process(LowStockEvent event) {
        List<Admin> stockAdmins = adminRepository.findAllByAdminRole(AdminRole.STOCK_ADMIN);
        if (stockAdmins.isEmpty()) {
            // 설정 누락이지 일시적 오류가 아니므로 nak()으로 재시도해봤자 소용없다 - 로그만 남기고 정상 종료.
            log.warn("재고 부족 알림을 받을 재고 관리자(STOCK_ADMIN)가 없습니다. bookId={}", event.bookId());
            return;
        }
        for (Admin admin : stockAdmins) {
            emailNotificationSender.send(admin.getEmail(), "재고 부족 알림",
                    "도서 번호 " + event.bookId() + "의 재고가 부족합니다. 남은 수량=" + event.remainingQuantity()
                            + ", 안전재고=" + event.safetyStock());
        }
    }

    @PreDestroy
    public void shutdown() {
        if (dispatcher != null) {
            natsConnection.closeDispatcher(dispatcher);
        }
    }
}

package com.dev24.bookstore.purchase.event;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.dev24.bookstore.auth.domain.Customer;
import com.dev24.bookstore.auth.repository.CustomerRepository;
import com.dev24.bookstore.common.notification.EmailNotificationSender;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.core.instrument.MeterRegistry;
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

import java.io.IOException;

// OrderCompletedEventPublisher가 발행한 메시지를 별도로 구독해 비동기로 소비한다(발행자와 스레드/트랜잭션을 공유하지 않음).
// 적립금 지급 + 알림(시뮬레이션)을 수행하고, 실패하면 nak()으로 JetStream이 재전달하게 둔다.
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.nats.enabled", havingValue = "true")
public class OrderCompletedEventConsumer {

    private static final String SUBJECT = "orders.completed";
    private static final String DURABLE_NAME = "order-completed-consumer";
    private static final String EVENT_TAG_VALUE = "order-completed";

    // 적립금 정책: 결제금액의 1% 적립(단순화된 예시, 실제 정책은 등급별 차등 등으로 확장 가능)
    private static final int POINT_RATE_PERCENT = 1;

    private final Connection natsConnection;
    private final JetStream jetStream;
    private final CustomerRepository customerRepository;
    private final EmailNotificationSender emailNotificationSender;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    private Dispatcher dispatcher;
    private JetStreamSubscription subscription;

    @PostConstruct
    public void subscribe() throws IOException, JetStreamApiException {
        dispatcher = natsConnection.createDispatcher();
        // deliverGroup(큐 그룹) 없이 동일한 durable 이름을 여러 곳에서 구독하면, JetStream의 durable push
        // consumer는 동시에 구독자 하나만 허용하기 때문에 app이 2 replica로 뜰 때 두 번째 인스턴스가
        // [SUB-90012] Consumer is already bound to a subscription으로 기동 자체에 실패한다(실제로 겪음).
        // 큐 그룹으로 묶으면 같은 durable에 여러 인스턴스가 동시에 바인딩될 수 있고, NATS가 그 그룹 멤버들
        // 사이에 메시지를 나눠 전달한다(각 메시지는 그룹 내 한 인스턴스에만 전달 - at-least-once는 유지).
        PushSubscribeOptions options = PushSubscribeOptions.builder()
                .durable(DURABLE_NAME)
                .deliverGroup(DURABLE_NAME)
                .build();
        // autoAck=false - 처리 성공/실패에 따라 직접 ack/nak하기 위함
        subscription = jetStream.subscribe(SUBJECT, DURABLE_NAME, dispatcher, this::handle, false, options);
    }

    private void handle(Message message) {
        try {
            OrderCompletedEvent event = objectMapper.readValue(message.getData(), OrderCompletedEvent.class);
            process(event);
            message.ack(); // 메시지를 정상적으로 수신 및 처리 완료했음을 NATS 서버에 알리는 신호
            meterRegistry.counter("dev24.events.processed", "event", EVENT_TAG_VALUE, "result", "success").increment();
        } catch (Exception e) {
            log.error("OrderCompletedEvent 처리 실패 - NATS 재전달 대기", e);
            message.nak(); // 메시지를 받았지만 현재 처리할 수 없는 상태(오류 등). 메시지 처리를 포기하고 재전송(Redelivery)을 요청하는 신호
            meterRegistry.counter("dev24.events.processed", "event", EVENT_TAG_VALUE, "result", "failure").increment();
        }
    }

    // 단위 테스트 대상 - 실제 비즈니스 처리(적립금 지급 + 알림)만 담당, Message/ack 관심사는 handle()이 처리
    void process(OrderCompletedEvent event) {
        Customer customer = customerRepository.findById(event.customerId())
                .orElseThrow(() -> new IllegalStateException("적립금 지급 대상 고객을 찾을 수 없습니다: " + event.customerId()));
        customer.addPoint(event.totalPrice() * POINT_RATE_PERCENT / 100);
        customerRepository.save(customer);

        emailNotificationSender.send(customer.getEmail(), "주문이 완료되었습니다",
                "주문번호 " + event.purchaseId() + "이 완료되어 " + (event.totalPrice() * POINT_RATE_PERCENT / 100)
                        + "포인트가 적립되었습니다.");
    }

    @PreDestroy
    public void shutdown() {
        if (dispatcher != null) {
            natsConnection.closeDispatcher(dispatcher);
        }
    }
}

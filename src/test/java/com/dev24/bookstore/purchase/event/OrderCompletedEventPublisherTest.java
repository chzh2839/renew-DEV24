package com.dev24.bookstore.purchase.event;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.nats.client.JetStream;

@ExtendWith(MockitoExtension.class)
class OrderCompletedEventPublisherTest {

    @Mock
    private JetStream jetStream;
    @Mock
    private ObjectMapper objectMapper;

    private OrderCompletedEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new OrderCompletedEventPublisher(jetStream, objectMapper);
    }

    // 정상 발행 시 정확한 subject("orders.completed")와 직렬화된 이벤트 바이트로 JetStream.publish가
    // 호출되는지 검증 - 컨슈머(OrderCompletedEventConsumer)가 이 subject를 구독하므로 값이 틀리면 소비되지 않는다.
    @Test
    void publish_success_sendsSerializedPayloadToOrdersCompletedSubject() throws Exception {
        OrderCompletedEvent event = new OrderCompletedEvent(1L, 2L, 30000, LocalDateTime.now());
        byte[] payload = "serialized".getBytes();
        given(objectMapper.writeValueAsBytes(event)).willReturn(payload);

        publisher.publish(event);

        verify(jetStream).publish(eq("orders.completed"), eq(payload));
    }

    // 주문 자체는 이미 커밋된 뒤라, NATS 발행이 실패해도 주문을 되돌리지 않고 예외를 삼킨 채 로그만 남긴다
    // (OrderCompletedEventPublisher 주석의 트레이드오프 - README 참고).
    @Test
    void publish_jetStreamThrows_doesNotPropagateException() throws Exception {
        OrderCompletedEvent event = new OrderCompletedEvent(1L, 2L, 30000, LocalDateTime.now());
        given(objectMapper.writeValueAsBytes(event)).willReturn("serialized".getBytes());
        given(jetStream.publish(any(String.class), any(byte[].class))).willThrow(new IOException("NATS unreachable"));

        assertThatCode(() -> publisher.publish(event)).doesNotThrowAnyException();
    }
}

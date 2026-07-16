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
class LowStockEventPublisherTest {

    @Mock
    private JetStream jetStream;
    @Mock
    private ObjectMapper objectMapper;

    private LowStockEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new LowStockEventPublisher(jetStream, objectMapper);
    }

    // 정상 발행 시 정확한 subject("orders.low-stock")와 직렬화된 이벤트 바이트로 JetStream.publish가
    // 호출되는지 검증 - 컨슈머(LowStockEventConsumer)가 이 subject를 구독하므로 값이 틀리면 소비되지 않는다.
    @Test
    void publish_success_sendsSerializedPayloadToOrdersLowStockSubject() throws Exception {
        LowStockEvent event = new LowStockEvent(10L, 2, 3, LocalDateTime.now());
        byte[] payload = "serialized".getBytes();
        given(objectMapper.writeValueAsBytes(event)).willReturn(payload);

        publisher.publish(event);

        verify(jetStream).publish(eq("orders.low-stock"), eq(payload));
    }

    // 재고 차감 자체는 이미 커밋된 뒤라, NATS 발행이 실패해도 되돌리지 않고 예외를 삼킨 채 로그만 남긴다
    // (LowStockEventPublisher 주석의 트레이드오프).
    @Test
    void publish_jetStreamThrows_doesNotPropagateException() throws Exception {
        LowStockEvent event = new LowStockEvent(10L, 2, 3, LocalDateTime.now());
        given(objectMapper.writeValueAsBytes(event)).willReturn("serialized".getBytes());
        given(jetStream.publish(any(String.class), any(byte[].class))).willThrow(new IOException("NATS unreachable"));

        assertThatCode(() -> publisher.publish(event)).doesNotThrowAnyException();
    }
}

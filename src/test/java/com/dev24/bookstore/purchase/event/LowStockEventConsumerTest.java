package com.dev24.bookstore.purchase.event;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.dev24.bookstore.auth.domain.Admin;
import com.dev24.bookstore.auth.repository.AdminRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.nats.client.Connection;
import io.nats.client.JetStream;

@ExtendWith(MockitoExtension.class)
class LowStockEventConsumerTest {

    @Mock
    private Connection natsConnection;
    @Mock
    private JetStream jetStream;
    @Mock
    private AdminRepository adminRepository;
    @Mock
    private ObjectMapper objectMapper;

    private LowStockEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new LowStockEventConsumer(natsConnection, jetStream, adminRepository, objectMapper);
    }

    private Admin admin(long id) {
        Admin admin = new Admin("admin1", "encoded", "관리자");
        ReflectionTestUtils.setField(admin, "id", id);
        return admin;
    }

    // 알림 대상 관리자가 존재하면 예외 없이 처리되는지 검증(실제 알림은 로그로 시뮬레이션)
    @Test
    void process_knownAdmin_doesNotThrow() {
        given(adminRepository.findById(1L)).willReturn(Optional.of(admin(1L)));
        LowStockEvent event = new LowStockEvent(10L, 1L, 2, 3, LocalDateTime.now());

        assertThatCode(() -> consumer.process(event)).doesNotThrowAnyException();
    }

    // 존재하지 않는 관리자면 예외를 던져 handle()이 nak()으로 재전달을 유도할 수 있게 하는지 검증
    @Test
    void process_unknownAdmin_throws() {
        given(adminRepository.findById(99L)).willReturn(Optional.empty());
        LowStockEvent event = new LowStockEvent(10L, 99L, 2, 3, LocalDateTime.now());

        assertThatThrownBy(() -> consumer.process(event))
                .isInstanceOf(IllegalStateException.class);
    }
}

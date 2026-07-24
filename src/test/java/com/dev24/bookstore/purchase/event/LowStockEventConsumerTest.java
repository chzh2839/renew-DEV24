package com.dev24.bookstore.purchase.event;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.dev24.bookstore.auth.domain.Admin;
import com.dev24.bookstore.auth.domain.AdminRole;
import com.dev24.bookstore.auth.repository.AdminRepository;
import com.dev24.bookstore.common.notification.EmailNotificationSender;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
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
    private EmailNotificationSender emailNotificationSender;
    @Mock
    private ObjectMapper objectMapper;

    private LowStockEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new LowStockEventConsumer(
                natsConnection, jetStream, adminRepository, emailNotificationSender, objectMapper,
                new SimpleMeterRegistry());
    }

    private Admin stockAdmin(long id, String email) {
        Admin admin = new Admin("stock-admin" + id, "encoded", "재고관리자" + id, email, AdminRole.STOCK_ADMIN);
        ReflectionTestUtils.setField(admin, "id", id);
        return admin;
    }

    // 재고 관리자(STOCK_ADMIN)가 여러 명이면 각각에게 알림 이메일이 발송되는지 검증
    @Test
    void process_notifiesEveryStockAdmin() {
        given(adminRepository.findAllByAdminRole(AdminRole.STOCK_ADMIN))
                .willReturn(List.of(stockAdmin(1L, "admin1@example.com"), stockAdmin(2L, "admin2@example.com")));
        LowStockEvent event = new LowStockEvent(10L, 2, 3, LocalDateTime.now());

        consumer.process(event);

        verify(emailNotificationSender).send(eq("admin1@example.com"), any(), any());
        verify(emailNotificationSender).send(eq("admin2@example.com"), any(), any());
        verify(emailNotificationSender, times(2)).send(any(), any(), any());
    }

    // 재고 관리자가 한 명도 없으면 예외 없이 종료하고, 이메일도 전혀 발송되지 않는지 검증
    @Test
    void process_noStockAdmins_doesNotThrowAndDoesNotEmail() {
        given(adminRepository.findAllByAdminRole(AdminRole.STOCK_ADMIN)).willReturn(List.of());
        LowStockEvent event = new LowStockEvent(10L, 2, 3, LocalDateTime.now());

        assertThatCode(() -> consumer.process(event)).doesNotThrowAnyException();

        verify(emailNotificationSender, never()).send(any(), any(), any());
    }
}

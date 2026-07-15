package com.dev24.bookstore.purchase.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dev24.bookstore.auth.domain.Customer;
import com.dev24.bookstore.auth.repository.CustomerRepository;
import com.dev24.bookstore.common.notification.EmailNotificationSender;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.nats.client.Connection;
import io.nats.client.JetStream;

@ExtendWith(MockitoExtension.class)
class OrderCompletedEventConsumerTest {

    @Mock
    private Connection natsConnection;
    @Mock
    private JetStream jetStream;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private EmailNotificationSender emailNotificationSender;
    @Mock
    private ObjectMapper objectMapper;

    private OrderCompletedEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new OrderCompletedEventConsumer(
                natsConnection, jetStream, customerRepository, emailNotificationSender, objectMapper);
    }

    private Customer customer(long id) {
        Customer customer = new Customer("customer1", "encoded", "홍길동", "길동이",
                "customer1@example.com", "010-0000-0000", "서울시", "소설", false);
        org.springframework.test.util.ReflectionTestUtils.setField(customer, "id", id);
        return customer;
    }

    // 결제금액의 1%가 적립되고, 변경된 Customer가 저장되고, 고객 이메일로 알림이 발송되는지 검증
    @Test
    void process_grantsOnePercentOfTotalPriceAsPointAndEmailsCustomer() {
        Customer customer = customer(1L);
        given(customerRepository.findById(1L)).willReturn(Optional.of(customer));
        OrderCompletedEvent event = new OrderCompletedEvent(10L, 1L, 20000, LocalDateTime.now());

        consumer.process(event);

        assertThat(customer.getPoint()).isEqualTo(200);
        verify(customerRepository).save(customer);
        verify(emailNotificationSender).send(anyString(), anyString(), anyString());
    }

    // 존재하지 않는 고객이면 예외를 던지고, 이메일도 발송되지 않는지 검증
    @Test
    void process_unknownCustomer_throwsAndDoesNotEmail() {
        given(customerRepository.findById(99L)).willReturn(Optional.empty());
        OrderCompletedEvent event = new OrderCompletedEvent(10L, 99L, 20000, LocalDateTime.now());

        assertThatThrownBy(() -> consumer.process(event))
                .isInstanceOf(IllegalStateException.class);

        verify(emailNotificationSender, never()).send(any(), any(), any());
    }
}

package com.dev24.bookstore.purchase.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
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
    private ObjectMapper objectMapper;

    private OrderCompletedEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new OrderCompletedEventConsumer(natsConnection, jetStream, customerRepository, objectMapper);
    }

    private Customer customer(long id) {
        Customer customer = new Customer("customer1", "encoded", "홍길동", "길동이",
                "customer1@example.com", "010-0000-0000", "서울시", "소설", false);
        org.springframework.test.util.ReflectionTestUtils.setField(customer, "id", id);
        return customer;
    }

    // 결제금액의 1%가 적립되고, 변경된 Customer가 저장되는지 검증
    @Test
    void process_grantsOnePercentOfTotalPriceAsPoint() {
        Customer customer = customer(1L);
        given(customerRepository.findById(1L)).willReturn(Optional.of(customer));
        OrderCompletedEvent event = new OrderCompletedEvent(10L, 1L, 20000, LocalDateTime.now());

        consumer.process(event);

        assertThat(customer.getPoint()).isEqualTo(200);
        verify(customerRepository).save(customer);
    }

    // 존재하지 않는 고객이면 예외를 던져 handle()이 nak()으로 재전달을 유도할 수 있게 하는지 검증
    @Test
    void process_unknownCustomer_throws() {
        given(customerRepository.findById(99L)).willReturn(Optional.empty());
        OrderCompletedEvent event = new OrderCompletedEvent(10L, 99L, 20000, LocalDateTime.now());

        assertThatThrownBy(() -> consumer.process(event))
                .isInstanceOf(IllegalStateException.class);
    }
}

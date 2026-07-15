package com.dev24.bookstore.common.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.dev24.bookstore.common.notification.domain.EmailNotificationHistory;
import com.dev24.bookstore.common.notification.domain.EmailNotificationStatus;
import com.dev24.bookstore.common.notification.repository.EmailNotificationHistoryRepository;

@ExtendWith(MockitoExtension.class)
class EmailNotificationSenderTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailNotificationHistoryRepository historyRepository;

    private EmailNotificationSender sender;

    @BeforeEach
    void setUp() {
        sender = new EmailNotificationSender(mailSender, historyRepository);
    }

    // 정상적인 수신자면 실제로 mailSender.send()가 호출되는지 검증
    @Test
    void send_validRecipient_sendsMail() {
        sender.send("customer1@example.com", "제목", "본문");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    // 수신자가 null이면 발송 자체를 시도하지 않는지 검증(관리자에게 이메일이 없는 경우 등)
    @Test
    void send_nullRecipient_doesNotSend() {
        sender.send(null, "제목", "본문");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    // 수신자가 빈 문자열이어도 발송을 시도하지 않는지 검증
    @Test
    void send_blankRecipient_doesNotSend() {
        sender.send("  ", "제목", "본문");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    // mailSender가 예외를 던져도 호출부(컨슈머)로 전파되지 않는지 검증 - 전파되면 NATS 재전달로 핵심 처리가
    // 중복 실행될 수 있어서 반드시 여기서 삼켜야 한다
    @Test
    void send_mailSenderThrows_doesNotPropagate() {
        willThrow(new MailSendException("연결 실패")).given(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> sender.send("customer1@example.com", "제목", "본문")).doesNotThrowAnyException();
    }

    // 정상 발송되면 SUCCESS 상태로 이력이 저장되는지 검증
    @Test
    void send_validRecipient_savesSuccessHistory() {
        sender.send("customer1@example.com", "제목", "본문");

        ArgumentCaptor<EmailNotificationHistory> captor = ArgumentCaptor.forClass(EmailNotificationHistory.class);
        verify(historyRepository).save(captor.capture());
        EmailNotificationHistory history = captor.getValue();
        assertThat(history.getToEmail()).isEqualTo("customer1@example.com");
        assertThat(history.getStatus()).isEqualTo(EmailNotificationStatus.SUCCESS);
        assertThat(history.getErrorMessage()).isNull();
    }

    // mailSender가 예외를 던지면 FAILED 상태 + 실패 사유로 이력이 저장되는지 검증
    @Test
    void send_mailSenderThrows_savesFailureHistory() {
        willThrow(new MailSendException("연결 실패")).given(mailSender).send(any(SimpleMailMessage.class));

        sender.send("customer1@example.com", "제목", "본문");

        ArgumentCaptor<EmailNotificationHistory> captor = ArgumentCaptor.forClass(EmailNotificationHistory.class);
        verify(historyRepository).save(captor.capture());
        EmailNotificationHistory history = captor.getValue();
        assertThat(history.getStatus()).isEqualTo(EmailNotificationStatus.FAILED);
        assertThat(history.getErrorMessage()).isEqualTo("연결 실패");
    }
}

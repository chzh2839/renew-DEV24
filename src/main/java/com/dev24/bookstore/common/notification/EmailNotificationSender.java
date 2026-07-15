package com.dev24.bookstore.common.notification;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.dev24.bookstore.common.notification.domain.EmailNotificationHistory;
import com.dev24.bookstore.common.notification.repository.EmailNotificationHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 구매완료/재고부족 등 여러 이벤트 컨슈머가 공유하는 이메일 발송 창구.
// 발송 실패를 여기서 삼키는 이유: 호출부(예: OrderCompletedEventConsumer)는 이미 적립금 지급 같은 핵심 처리를 커밋한 "뒤"에 이 메서드를 호출한다.
// 예외가 handle()까지 전파되면 NATS가 메시지를 재전달해 핵심 처리가 중복 실행될 수 있다(멱등성 키 없음)
//  - 그래서 알림 발송 실패는 핵심 처리와 분리해 로그만 남긴다.
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationSender {

    private final JavaMailSender mailSender;
    private final EmailNotificationHistoryRepository historyRepository;

    public void send(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            log.warn("이메일 주소가 없어 알림을 보내지 않습니다. subject={}", subject);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            saveHistory(EmailNotificationHistory.success(to, subject, body));
        } catch (Exception e) {
            log.error("이메일 발송 실패, to={}, subject={}", to, subject, e);
            saveHistory(EmailNotificationHistory.failure(to, subject, body, e.getMessage()));
        }
    }

    // 이력 저장 실패가 이미 끝난 발송 결과(성공/실패)에 영향을 주면 안 되므로 별도로 삼긴다.
    private void saveHistory(EmailNotificationHistory history) {
        try {
            historyRepository.save(history);
        } catch (Exception e) {
            log.error("이메일 발송 이력 저장 실패, to={}, subject={}", history.getToEmail(), history.getSubject(), e);
        }
    }
}

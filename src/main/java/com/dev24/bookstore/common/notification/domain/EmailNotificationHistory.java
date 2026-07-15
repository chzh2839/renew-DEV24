package com.dev24.bookstore.common.notification.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// EmailNotificationSender가 실제로 발송을 "시도"했을 때만 남기는 이력(수신자가 없어 시도 자체를 안 한 경우는 제외).
// to_email은 Customer/Admin 어느 쪽이든 될 수 있는 범용 발송 창구라 FK가 아니라 발송 시점의 이메일 문자열을 그대로 남긴다
// - 나중에 수신자가 이메일을 바꿔도 이력이 왜곡되지 않는다.
@Entity
@Table(name = "email_notification_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailNotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "to_email", nullable = false, length = 100)
    private String toEmail;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "body", nullable = false)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmailNotificationStatus status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    private EmailNotificationHistory(String toEmail, String subject, String body, EmailNotificationStatus status,
            String errorMessage) {
        this.toEmail = toEmail;
        this.subject = subject;
        this.body = body;
        this.status = status;
        this.errorMessage = errorMessage;
        this.sentAt = LocalDateTime.now();
    }

    public static EmailNotificationHistory success(String toEmail, String subject, String body) {
        return new EmailNotificationHistory(toEmail, subject, body, EmailNotificationStatus.SUCCESS, null);
    }

    public static EmailNotificationHistory failure(String toEmail, String subject, String body,
            String errorMessage) {
        return new EmailNotificationHistory(toEmail, subject, body, EmailNotificationStatus.FAILED, errorMessage);
    }
}

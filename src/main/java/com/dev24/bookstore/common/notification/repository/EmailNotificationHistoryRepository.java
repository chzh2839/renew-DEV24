package com.dev24.bookstore.common.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev24.bookstore.common.notification.domain.EmailNotificationHistory;

public interface EmailNotificationHistoryRepository extends JpaRepository<EmailNotificationHistory, Long> {
}

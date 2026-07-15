package com.dev24.bookstore.auth.domain;

// 관리자의 업무 분류(알림 대상 그룹 등)를 위한 비즈니스 도메인 구분이다.
// Spring Security 인가에 쓰이는 Role(CUSTOMER/ADMIN)과는 완전히 별개 - 혼동하지 말 것.
// 예: 재고 부족 알림은 STOCK_ADMIN 전원에게 발송되지만, 인가 체크(@PreAuthorize("hasRole('ADMIN')"))는
// 그대로 Admin.getRole() == Role.ADMIN에 의해 처리된다.
public enum AdminRole {
    GENERAL,
    STOCK_ADMIN
}

package com.dev24.bookstore.auth.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "interest", length = 255)
    private String interest;

    @Column(name = "newsletter_yn", nullable = false)
    private boolean newsletterYn;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "point", nullable = false)
    private int point;

    public Customer(String loginId, String passwordHash, String name, String nickname,
                     String email, String phone, String address, String interest, boolean newsletterYn) {
        this.loginId = loginId;
        this.passwordHash = passwordHash;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.interest = interest;
        this.newsletterYn = newsletterYn;
        this.joinedAt = LocalDateTime.now();
    }

    public Role getRole() {
        return Role.CUSTOMER;
    }

    // 구매완료 이벤트 컨슈머(OrderCompletedEventConsumer)가 적립금 지급 시 호출
    public void addPoint(int amount) {
        this.point += amount;
    }
}

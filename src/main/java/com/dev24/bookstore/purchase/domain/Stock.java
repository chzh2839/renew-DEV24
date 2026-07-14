package com.dev24.bookstore.purchase.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.dev24.bookstore.auth.domain.Admin;
import com.dev24.bookstore.book.domain.Book;

@Entity
@Table(name = "stock")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 도서 1권당 재고 1행 - book 모듈은 이 관계를 모르는 단방향 참조(Book 쪽에 역방향 필드 없음)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false, unique = true)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "sale_price", nullable = false)
    private int salePrice;

    // 구매 가능 수량 = quantity - safetyStock 검증에 쓰이는 임계치(신규 필드, 레거시엔 없음)
    @Column(name = "safety_stock", nullable = false)
    private int safetyStock;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    // 동시 구매 시 오버셀 방지용 낙관적 락(신규, 레거시엔 없음)
    @Version
    @Column(name = "version", nullable = false)
    private int version;

    public Stock(Book book, Admin admin, int quantity, int salePrice, int safetyStock) {
        this.book = book;
        this.admin = admin;
        this.quantity = quantity;
        this.salePrice = salePrice;
        this.safetyStock = safetyStock;
        this.registeredAt = LocalDateTime.now();
    }
}

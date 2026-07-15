package com.dev24.bookstore.purchase.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.dev24.bookstore.auth.domain.Customer;
import com.dev24.bookstore.book.domain.Book;

@Entity
@Table(name = "cart")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    // 단가가 아닌 라인 합계(단가 × 수량) - 레거시 crt_price
    @Column(name = "price_snapshot", nullable = false)
    private int priceSnapshot;

    public Cart(Customer customer, Book book, int quantity, int priceSnapshot) {
        this.customer = customer;
        this.book = book;
        this.quantity = quantity;
        this.priceSnapshot = priceSnapshot;
    }

    // 이미 담긴 책을 또 담을 때(merge) 수량/라인합계를 더한다 - 재고 검증은 서비스 레이어의 책임(Stock과 동일 원칙)
    public void increaseQuantity(int amount, int additionalPrice) {
        this.quantity += amount;
        this.priceSnapshot += additionalPrice;
    }
}

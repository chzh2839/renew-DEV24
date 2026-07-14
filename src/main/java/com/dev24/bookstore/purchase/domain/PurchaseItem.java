package com.dev24.bookstore.purchase.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.purchase.domain.enums.PurchaseItemStatus;

@Entity
@Table(name = "purchase_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    // 이 라인의 가격 합계(단가 × 수량) - 레거시 pd_price는 배송비가 섞여 있었으나 재현하지 않음
    @Column(name = "price", nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_state", nullable = false, length = 20)
    private PurchaseItemStatus orderState;

    public PurchaseItem(Purchase purchase, Book book, int quantity, int price) {
        this.purchase = purchase;
        this.book = book;
        this.quantity = quantity;
        this.price = price;
        this.orderState = PurchaseItemStatus.PREPARING;
    }
}

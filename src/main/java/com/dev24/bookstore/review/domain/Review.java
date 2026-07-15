package com.dev24.bookstore.review.domain;

import java.time.LocalDateTime;

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

import com.dev24.bookstore.auth.domain.Customer;
import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.purchase.domain.PurchaseItem;

@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    // 실제로 구매한 사람만 리뷰를 남길 수 있는 구매 인증 리뷰 구조 - 구매 아이템 1개당 리뷰는 최대 1개(1:0~1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_item_id", nullable = false, unique = true)
    private PurchaseItem purchaseItem;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "content", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ReviewType type;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "written_at", nullable = false)
    private LocalDateTime writtenAt;

    public Review(Customer customer, Book book, PurchaseItem purchaseItem,
            int score, String content, ReviewType type, String imageUrl) {
        this.customer = customer;
        this.book = book;
        this.purchaseItem = purchaseItem;
        this.score = score;
        this.content = content;
        this.type = type;
        this.imageUrl = imageUrl;
        this.writtenAt = LocalDateTime.now();
    }
}

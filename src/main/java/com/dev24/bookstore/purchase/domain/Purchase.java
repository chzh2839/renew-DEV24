package com.dev24.bookstore.purchase.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.dev24.bookstore.auth.domain.Customer;
import com.dev24.bookstore.purchase.domain.enums.PaymentMethod;

@Entity
@Table(name = "purchase")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "sender_name", nullable = false, length = 100)
    private String senderName;

    @Column(name = "sender_phone", nullable = false, length = 20)
    private String senderPhone;

    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone;

    @Column(name = "zipcode", nullable = false, length = 10)
    private String zipcode;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "total_price", nullable = false)
    private int totalPrice;

    @Column(name = "purchased_at", nullable = false)
    private LocalDateTime purchasedAt;

    // PurchaseItem이 소유하는 FK(purchase_id) - 실제 초기화는 PurchaseItem 쪽에서 각각 save()로 이루어짐(cascade 없음),
    // Book-BookImage/Rating과 동일하게 조회 편의를 위한 매핑만.
    @OneToMany(mappedBy = "purchase")
    private List<PurchaseItem> items = new ArrayList<>();

    public Purchase(Customer customer, String senderName, String senderPhone, String receiverName,
                     String receiverPhone, String zipcode, String address, PaymentMethod paymentMethod,
                     int totalPrice) {
        this.customer = customer;
        this.senderName = senderName;
        this.senderPhone = senderPhone;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.zipcode = zipcode;
        this.address = address;
        this.paymentMethod = paymentMethod;
        this.totalPrice = totalPrice;
        this.purchasedAt = LocalDateTime.now();
    }
}

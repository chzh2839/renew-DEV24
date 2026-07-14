package com.dev24.bookstore.purchase.controller.response;

import java.time.LocalDateTime;

import com.dev24.bookstore.purchase.domain.Purchase;
import com.dev24.bookstore.purchase.domain.enums.PaymentMethod;

public record PurchaseResponse(
        Long id,
        int totalPrice,
        PaymentMethod paymentMethod,
        LocalDateTime purchasedAt) {

    public static PurchaseResponse from(Purchase purchase) {
        return new PurchaseResponse(
                purchase.getId(), purchase.getTotalPrice(), purchase.getPaymentMethod(), purchase.getPurchasedAt());
    }
}

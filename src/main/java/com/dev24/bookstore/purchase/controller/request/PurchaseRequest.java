package com.dev24.bookstore.purchase.controller.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.dev24.bookstore.purchase.domain.enums.PaymentMethod;

public record PurchaseRequest(
        @NotEmpty List<Long> cartItemIds,
        @NotBlank @Size(max = 100) String senderName,
        @NotBlank @Size(max = 20) String senderPhone,
        @NotBlank @Size(max = 100) String receiverName,
        @NotBlank @Size(max = 20) String receiverPhone,
        @NotBlank @Size(max = 10) String zipcode,
        @NotBlank @Size(max = 255) String address,
        @NotNull PaymentMethod paymentMethod) {
}

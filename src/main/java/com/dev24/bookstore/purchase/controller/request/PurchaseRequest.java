package com.dev24.bookstore.purchase.controller.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.dev24.bookstore.purchase.domain.enums.PaymentMethod;

public record PurchaseRequest(
        @NotEmpty List<Long> cartItemIds,
        @NotBlank @Size(max = 100) String senderName,

        @NotBlank
        @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$", message = "휴대폰 번호 형식이 올바르지 않습니다 (예: 010-1234-5678)")
        String senderPhone,

        @NotBlank @Size(max = 100) String receiverName,

        @NotBlank
        @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$", message = "휴대폰 번호 형식이 올바르지 않습니다 (예: 010-1234-5678)")
        String receiverPhone,

        @NotBlank
        @Pattern(regexp = "^\\d{5}$", message = "우편번호 형식이 올바르지 않습니다 (예: 06236)")
        String zipcode,

        @NotBlank @Size(max = 255) String address,
        @NotNull PaymentMethod paymentMethod) {
}

package com.dev24.bookstore.purchase.controller.response;

import com.dev24.bookstore.purchase.domain.Cart;

public record CartResponse(Long id, Long bookId, int quantity, int priceSnapshot) {

    public static CartResponse from(Cart cart) {
        return new CartResponse(cart.getId(), cart.getBook().getId(), cart.getQuantity(), cart.getPriceSnapshot());
    }
}

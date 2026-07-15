package com.dev24.bookstore.purchase.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dev24.bookstore.auth.domain.Customer;
import com.dev24.bookstore.auth.repository.CustomerRepository;
import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.repository.BookRepository;
import com.dev24.bookstore.common.exception.BusinessException;
import com.dev24.bookstore.common.exception.ErrorCode;
import com.dev24.bookstore.purchase.controller.request.CartAddRequest;
import com.dev24.bookstore.purchase.controller.response.CartResponse;
import com.dev24.bookstore.purchase.domain.Cart;
import com.dev24.bookstore.purchase.domain.Stock;
import com.dev24.bookstore.purchase.repository.CartRepository;
import com.dev24.bookstore.purchase.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartCommandService {

    private final CustomerRepository customerRepository;
    private final BookRepository bookRepository;
    private final StockRepository stockRepository;
    private final CartRepository cartRepository;

    // 이미 담긴 책이면 수량/금액을 합치고, 처음 담는 책이면 새 장바구니 행을 만든다.
    // 여기서 하는 재고 검증은 사용자에게 빠른 피드백을 주기 위한 사전 체크일 뿐 최종 판단이 아니다 - 실제 오버셀 방지는
    // purchase() 시점의 @Version 낙관적 락이 담당하므로, 담을 때와 구매할 때 사이의 재고 변동은 purchase()가 걸러낸다.
    @Transactional
    public CartResponse addToCart(String loginId, CartAddRequest request) {
        Customer customer = customerRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        Stock stock = stockRepository.findByBookId(book.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        Optional<Cart> existingCartItem = cartRepository.findByCustomerIdAndBookId(customer.getId(), book.getId());
        int quantityAfterAdd = existingCartItem.map(Cart::getQuantity).orElse(0) + request.quantity();
        if (stock.getQuantity() - stock.getSafetyStock() < quantityAfterAdd) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }

        int addedPrice = stock.getSalePrice() * request.quantity();
        Cart cartItem = existingCartItem
                .map(cart -> {
                    cart.increaseQuantity(request.quantity(), addedPrice);
                    return cart;
                })
                .orElseGet(() -> cartRepository.save(new Cart(customer, book, request.quantity(), addedPrice)));

        return CartResponse.from(cartItem);
    }
}

package com.dev24.bookstore.purchase.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dev24.bookstore.auth.domain.Customer;
import com.dev24.bookstore.auth.repository.CustomerRepository;
import com.dev24.bookstore.common.exception.BusinessException;
import com.dev24.bookstore.common.exception.ErrorCode;
import com.dev24.bookstore.purchase.controller.request.PurchaseRequest;
import com.dev24.bookstore.purchase.controller.response.PurchaseResponse;
import com.dev24.bookstore.purchase.domain.Cart;
import com.dev24.bookstore.purchase.domain.Purchase;
import com.dev24.bookstore.purchase.domain.PurchaseItem;
import com.dev24.bookstore.purchase.domain.Stock;
import com.dev24.bookstore.purchase.event.LowStockEvent;
import com.dev24.bookstore.purchase.event.OrderCompletedEvent;
import com.dev24.bookstore.purchase.repository.CartRepository;
import com.dev24.bookstore.purchase.repository.PurchaseItemRepository;
import com.dev24.bookstore.purchase.repository.PurchaseRepository;
import com.dev24.bookstore.purchase.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseCommandService {

    private final CustomerRepository customerRepository;
    private final CartRepository cartRepository;
    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final StockRepository stockRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    // 고객 검증 -> 장바구니 소유권 검증 -> 주문 헤더 생성 -> (재고 확인/차감 + 주문 라인 생성) x N -> 장바구니 삭제
    // 전체를 하나의 트랜잭션으로 묶는다 - 중간 어디서든 실패하면 이미 반영된 재고 차감/저장까지 전부 롤백된다.
    @Transactional
    public PurchaseResponse purchase(String loginId, PurchaseRequest request) {
        Customer customer = customerRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        List<Cart> cartItems = cartRepository.findAllById(request.cartItemIds());
        // 장바구니 소유권 검증
        if (cartItems.size() != request.cartItemIds().size()
                || cartItems.stream().anyMatch(cartItem -> !cartItem.getCustomer().getId().equals(customer.getId()))) {
            // 존재하지 않는 항목과 타인 소유 항목을 구분하지 않고 동일하게 404로 응답해 존재 여부 자체를 감춘다
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }

        int totalPrice = cartItems.stream().mapToInt(Cart::getPriceSnapshot).sum();
        // 주문 생성
        Purchase purchase = purchaseRepository.save(new Purchase(customer, request.senderName(), request.senderPhone(),
                request.receiverName(), request.receiverPhone(), request.zipcode(), request.address(),
                request.paymentMethod(), totalPrice));

        for (Cart cartItem : cartItems) {
            // 재고 확인 및 차감 - 구매 가능 수량은 재고 전량이 아닌 (재고 - 안전재고)
            Stock stock = stockRepository.findByBookId(cartItem.getBook().getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
            if (stock.getQuantity() - stock.getSafetyStock() < cartItem.getQuantity()) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
            }
            int quantityBeforeDecrease = stock.getQuantity();
            stock.decreaseQuantity(cartItem.getQuantity());
            purchaseItemRepository.save(
                    new PurchaseItem(purchase, cartItem.getBook(), cartItem.getQuantity(), cartItem.getPriceSnapshot()));

            // 안전재고 이하로 "떨어지는 순간"에만 발행 - 이미 안전재고 이하인 상태에서 추가 구매가 계속 들어와도
            // 재발행하지 않아 관리자에게 같은 알림이 반복되는 걸 막는다.
            if (quantityBeforeDecrease > stock.getSafetyStock() && stock.getQuantity() <= stock.getSafetyStock()) {
                applicationEventPublisher.publishEvent(new LowStockEvent(stock.getBook().getId(),
                        stock.getAdmin().getId(), stock.getQuantity(), stock.getSafetyStock(), LocalDateTime.now()));
            }
        }

        // 구매한 상품은 장바구니에서 삭제
        cartRepository.deleteAll(cartItems);

        // 적립금/알림은 이 트랜잭션의 원자성 대상이 아니다 - 커밋 성공이 보장된 뒤에만 NATS로 실제 발행되도록
        // OrderCompletedEventPublisher(@TransactionalEventListener(AFTER_COMMIT))에 위임한다.
        applicationEventPublisher.publishEvent(
                new OrderCompletedEvent(purchase.getId(), customer.getId(), totalPrice, purchase.getPurchasedAt()));

        return PurchaseResponse.from(purchase);
    }
}

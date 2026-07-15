package com.dev24.bookstore.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dev24.bookstore.auth.domain.Customer;
import com.dev24.bookstore.auth.repository.CustomerRepository;
import com.dev24.bookstore.common.exception.BusinessException;
import com.dev24.bookstore.common.exception.ErrorCode;
import com.dev24.bookstore.common.sanitizer.HtmlSanitizer;
import com.dev24.bookstore.purchase.domain.PurchaseItem;
import com.dev24.bookstore.purchase.repository.PurchaseItemRepository;
import com.dev24.bookstore.review.controller.request.ReviewCreateRequest;
import com.dev24.bookstore.review.controller.request.ReviewUpdateRequest;
import com.dev24.bookstore.review.controller.response.ReviewResponse;
import com.dev24.bookstore.review.domain.Review;
import com.dev24.bookstore.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewCommandService {

    private final CustomerRepository customerRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final ReviewRepository reviewRepository;
    private final HtmlSanitizer htmlSanitizer;

    // 실제로 그 상품을 구매한 본인만 리뷰를 남길 수 있다(구매 인증 리뷰) - 존재하지 않는 purchaseItemId와
    // 남의 purchaseItemId를 구분하지 않고 동일하게 404로 응답해(PurchaseCommandService와 동일 원칙)
    // 타인 구매 내역의 존재 여부 자체를 감춘다.
    @Transactional
    public ReviewResponse createReview(String loginId, ReviewCreateRequest request) {
        Customer customer = customerRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        PurchaseItem purchaseItem = purchaseItemRepository.findById(request.purchaseItemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (!purchaseItem.getPurchase().getCustomer().getId().equals(customer.getId())) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }

        Review review = reviewRepository.save(new Review(customer, purchaseItem.getBook(), purchaseItem,
                request.score(), htmlSanitizer.sanitize(request.content()), request.type(), request.imageUrl()));
        return ReviewResponse.from(review);
    }

    // 리뷰 작성자 본인만 수정할 수 있다 - 타인 리뷰면 404(동일 원칙)
    @Transactional
    public ReviewResponse updateReview(String loginId, Long reviewId, ReviewUpdateRequest request) {
        Customer customer = customerRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (!review.getCustomer().getId().equals(customer.getId())) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }

        review.update(request.score(), htmlSanitizer.sanitize(request.content()), request.type(), request.imageUrl());
        return ReviewResponse.from(review);
    }

    // 리뷰 작성자 본인만 삭제할 수 있다 - 타인 리뷰면 404(동일 원칙)
    @Transactional
    public void deleteReview(String loginId, Long reviewId) {
        Customer customer = customerRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (!review.getCustomer().getId().equals(customer.getId())) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }

        reviewRepository.delete(review);
    }
}

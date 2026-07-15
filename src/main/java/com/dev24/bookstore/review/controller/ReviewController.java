package com.dev24.bookstore.review.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dev24.bookstore.common.response.ApiResponse;
import com.dev24.bookstore.review.controller.request.ReviewCreateRequest;
import com.dev24.bookstore.review.controller.request.ReviewUpdateRequest;
import com.dev24.bookstore.review.controller.response.ReviewResponse;
import com.dev24.bookstore.review.service.ReviewCommandService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewCommandService reviewCommandService;

    // 고객 전용 - loginId는 JwtAuthenticationFilter가 SecurityContext에 세팅한 인증 정보에서 꺼낸다.
    // Authentication을 컨트롤러 파라미터로 직접 받으면 MockMvc(addFilters=false) 슬라이스 테스트에서
    // request.getUserPrincipal()이 비어 있어 인자 해석이 실패하므로, SecurityContextHolder에서 직접 읽는다(다른 컨트롤러와 동일).
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<ReviewResponse> create(@Valid @RequestBody ReviewCreateRequest request) {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(reviewCommandService.createReview(loginId, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<ReviewResponse> update(@PathVariable Long id, @Valid @RequestBody ReviewUpdateRequest request) {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(reviewCommandService.updateReview(loginId, id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        reviewCommandService.deleteReview(loginId, id);
        return ApiResponse.success();
    }
}

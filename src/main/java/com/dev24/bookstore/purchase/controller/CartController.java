package com.dev24.bookstore.purchase.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dev24.bookstore.common.response.ApiResponse;
import com.dev24.bookstore.purchase.controller.request.CartAddRequest;
import com.dev24.bookstore.purchase.controller.response.CartResponse;
import com.dev24.bookstore.purchase.service.CartCommandService;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Tag(name = "장바구니", description = "장바구니 담기")
public class CartController {

    private final CartCommandService cartCommandService;

    // 고객 전용 - loginId는 JwtAuthenticationFilter가 SecurityContext에 세팅한 인증 정보에서 꺼낸다.
    // Authentication을 컨트롤러 파라미터로 직접 받으면 MockMvc(addFilters=false) 슬라이스 테스트에서
    // request.getUserPrincipal()이 비어 있어 인자 해석이 실패하므로, SecurityContextHolder에서 직접 읽는다(PurchaseController와 동일).
    @Operation(summary = "장바구니 담기", description = "고객 전용")
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<CartResponse> add(@Valid @RequestBody CartAddRequest request) {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(cartCommandService.addToCart(loginId, request));
    }
}

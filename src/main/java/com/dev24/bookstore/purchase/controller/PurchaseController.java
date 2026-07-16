package com.dev24.bookstore.purchase.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dev24.bookstore.common.response.ApiResponse;
import com.dev24.bookstore.purchase.controller.request.PurchaseRequest;
import com.dev24.bookstore.purchase.controller.response.PurchaseResponse;
import com.dev24.bookstore.purchase.service.PurchaseCommandService;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
@Tag(name = "구매", description = "구매 확정")
public class PurchaseController {

    private final PurchaseCommandService purchaseCommandService;

    // 고객 전용 - loginId는 JwtAuthenticationFilter가 SecurityContext에 세팅한 인증 정보에서 꺼낸다.
    // Authentication을 컨트롤러 파라미터로 직접 받으면 MockMvc(addFilters=false) 슬라이스 테스트에서
    // request.getUserPrincipal()이 비어 있어 인자 해석이 실패하므로, SecurityContextHolder에서 직접 읽는다.
    @Operation(summary = "구매 확정", description = "고객 전용. 장바구니 항목을 구매로 전환하고 재고 차감")
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<PurchaseResponse> purchase(@Valid @RequestBody PurchaseRequest request) {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(purchaseCommandService.purchase(loginId, request));
    }
}

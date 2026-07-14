package com.dev24.bookstore.purchase.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.dev24.bookstore.auth.security.JwtAuthenticationFilter;
import com.dev24.bookstore.common.config.SecurityConfig;
import com.dev24.bookstore.common.exception.BusinessException;
import com.dev24.bookstore.common.exception.ErrorCode;
import com.dev24.bookstore.purchase.controller.request.PurchaseRequest;
import com.dev24.bookstore.purchase.controller.response.PurchaseResponse;
import com.dev24.bookstore.purchase.domain.enums.PaymentMethod;
import com.dev24.bookstore.purchase.service.PurchaseCommandService;

// @WebMvcTest는 JwtAuthenticationFilter도 스캔하므로 addFilters=false에서도 빈 생성을 위해 목 처리가 필요하고,
// @PreAuthorize 인터셉터가 동작하려면 SecurityConfig를 Import해야 한다 (BookControllerTest/AuthControllerTest와 동일 패턴).
@WebMvcTest(controllers = PurchaseController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
class PurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PurchaseCommandService purchaseCommandService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String REQUEST_JSON = """
            {"cartItemIds":[1,2],"senderName":"홍길동","senderPhone":"010-1111-1111",
             "receiverName":"홍길동","receiverPhone":"010-1111-1111","zipcode":"06236",
             "address":"서울시 강남구","paymentMethod":"CREDIT_CARD"}
            """;

    // 고객으로 인증된 요청이면 구매가 성공하고 PurchaseResponse가 내려오는지 검증
    @Test
    @WithMockUser(username = "customer1", roles = "CUSTOMER")
    void purchase_asCustomer_returnsPurchaseResponse() throws Exception {
        given(purchaseCommandService.purchase(eq("customer1"), any(PurchaseRequest.class)))
                .willReturn(new PurchaseResponse(1L, 20000, PaymentMethod.CREDIT_CARD, LocalDateTime.now()));

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalPrice").value(20000));
    }

    // @PreAuthorize("hasRole('CUSTOMER')")가 관리자 요청을 403 + A004로 거부하는지 검증
    @Test
    @WithMockUser(roles = "ADMIN")
    void purchase_asAdmin_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("A004"));
    }

    // 익명 사용자(미인증)도 CUSTOMER 권한이 없으므로 403 + A004로 거부되는지 검증
    @Test
    @WithAnonymousUser
    void purchase_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("A004"));
    }

    // cartItemIds가 비어있으면 @Valid 검증에서 걸려 서비스까지 가지 않고 400 + C001로 응답하는지 검증
    @Test
    @WithMockUser(username = "customer1", roles = "CUSTOMER")
    void purchase_emptyCartItemIds_returnsInvalidInputError() throws Exception {
        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cartItemIds":[],"senderName":"홍길동","senderPhone":"010-1111-1111",
                                 "receiverName":"홍길동","receiverPhone":"010-1111-1111","zipcode":"06236",
                                 "address":"서울시 강남구","paymentMethod":"CREDIT_CARD"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C001"));
    }

    // 서비스가 재고 부족으로 BusinessException(INSUFFICIENT_STOCK)을 던지면 409 + P001로 응답하는지 검증
    @Test
    @WithMockUser(username = "customer1", roles = "CUSTOMER")
    void purchase_insufficientStock_returnsConflict() throws Exception {
        given(purchaseCommandService.purchase(eq("customer1"), any(PurchaseRequest.class)))
                .willThrow(new BusinessException(ErrorCode.INSUFFICIENT_STOCK));

        mockMvc.perform(post("/api/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("P001"));
    }
}

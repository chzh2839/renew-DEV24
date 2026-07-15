package com.dev24.bookstore.purchase.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.dev24.bookstore.purchase.controller.request.CartAddRequest;
import com.dev24.bookstore.purchase.controller.response.CartResponse;
import com.dev24.bookstore.purchase.service.CartCommandService;

// @WebMvcTest는 JwtAuthenticationFilter도 스캔하므로 addFilters=false에서도 빈 생성을 위해 목 처리가 필요하고,
// @PreAuthorize 인터셉터가 동작하려면 SecurityConfig를 Import해야 한다 (PurchaseControllerTest와 동일 패턴).
@WebMvcTest(controllers = CartController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartCommandService cartCommandService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String REQUEST_JSON = """
            {"bookId":1,"quantity":2}
            """;

    // 고객으로 인증된 요청이면 장바구니 담기가 성공하고 CartResponse가 내려오는지 검증
    @Test
    @WithMockUser(username = "customer1", roles = "CUSTOMER")
    void add_asCustomer_returnsCartResponse() throws Exception {
        given(cartCommandService.addToCart(eq("customer1"), any(CartAddRequest.class)))
                .willReturn(new CartResponse(1L, 1L, 2, 20000));

        mockMvc.perform(post("/api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.quantity").value(2));
    }

    // @PreAuthorize("hasRole('CUSTOMER')")가 관리자 요청을 403 + A004로 거부하는지 검증
    @Test
    @WithMockUser(roles = "ADMIN")
    void add_asAdmin_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("A004"));
    }

    // 익명 사용자(미인증)도 CUSTOMER 권한이 없으므로 403 + A004로 거부되는지 검증
    @Test
    @WithAnonymousUser
    void add_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("A004"));
    }

    // quantity가 0 이하면 @Valid 검증에서 걸려 서비스까지 가지 않고 400 + C001로 응답하는지 검증
    @Test
    @WithMockUser(username = "customer1", roles = "CUSTOMER")
    void add_nonPositiveQuantity_returnsInvalidInputError() throws Exception {
        mockMvc.perform(post("/api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bookId":1,"quantity":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C001"));
    }

    // 서비스가 재고 부족으로 BusinessException(INSUFFICIENT_STOCK)을 던지면 409 + P001로 응답하는지 검증
    @Test
    @WithMockUser(username = "customer1", roles = "CUSTOMER")
    void add_insufficientStock_returnsConflict() throws Exception {
        given(cartCommandService.addToCart(eq("customer1"), any(CartAddRequest.class)))
                .willThrow(new BusinessException(ErrorCode.INSUFFICIENT_STOCK));

        mockMvc.perform(post("/api/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("P001"));
    }
}

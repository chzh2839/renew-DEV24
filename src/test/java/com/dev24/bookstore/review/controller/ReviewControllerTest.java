package com.dev24.bookstore.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.dev24.bookstore.auth.security.JwtAuthenticationFilter;
import com.dev24.bookstore.common.config.SecurityConfig;
import com.dev24.bookstore.common.exception.BusinessException;
import com.dev24.bookstore.common.exception.ErrorCode;
import com.dev24.bookstore.review.controller.request.PresignedUploadRequest;
import com.dev24.bookstore.review.controller.request.ReviewCreateRequest;
import com.dev24.bookstore.review.controller.request.ReviewUpdateRequest;
import com.dev24.bookstore.review.controller.response.PresignedUploadResponse;
import com.dev24.bookstore.review.controller.response.ReviewResponse;
import com.dev24.bookstore.review.domain.ReviewType;
import com.dev24.bookstore.review.service.ReviewCommandService;
import com.dev24.bookstore.review.service.ReviewImageService;
import com.dev24.bookstore.review.service.ReviewQueryService;

// @WebMvcTest는 JwtAuthenticationFilter도 스캔하므로 addFilters=false에서도 빈 생성을 위해 목 처리가 필요하고,
// @PreAuthorize 인터셉터가 동작하려면 SecurityConfig를 Import해야 한다 (PurchaseControllerTest/CartControllerTest와 동일 패턴).
@WebMvcTest(controllers = ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewCommandService reviewCommandService;
    @MockitoBean
    private ReviewQueryService reviewQueryService;
    @MockitoBean
    private ReviewImageService reviewImageService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String CREATE_REQUEST_JSON = """
            {"purchaseItemId":1,"score":5,"content":"정말 좋아요","type":"TEXT"}
            """;

    private static final String UPDATE_REQUEST_JSON = """
            {"score":4,"content":"괜찮아요","type":"TEXT"}
            """;

    private ReviewResponse reviewResponse(int score, String content) {
        return new ReviewResponse(1L, 1L, 1L, score, content, ReviewType.TEXT, null, LocalDateTime.now());
    }

    // 도서 상세 페이지에서 누구나(비로그인 포함) 리뷰 목록을 조회할 수 있는지 검증 - @PreAuthorize가 없는 공개 엔드포인트라
    // 별도 인증 어노테이션 없이 호출(BookControllerTest.search_returnsPagedBooks와 동일 패턴)
    @Test
    void list_publicAccess_returnsPagedReviews() throws Exception {
        var page = new PageImpl<>(List.of(reviewResponse(5, "정말 좋아요")), PageRequest.of(0, 20), 1);
        given(reviewQueryService.getReviews(eq(1L), any())).willReturn(page);

        mockMvc.perform(get("/api/reviews").param("bookId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].content").value("정말 좋아요"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    // 고객으로 인증된 요청이면 리뷰 작성이 성공하고 ReviewResponse가 내려오는지 검증
    @Test
    @WithMockUser(username = "customer1", roles = "CUSTOMER")
    void create_asCustomer_returnsReviewResponse() throws Exception {
        given(reviewCommandService.createReview(eq("customer1"), any(ReviewCreateRequest.class)))
                .willReturn(reviewResponse(5, "정말 좋아요"));

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_REQUEST_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").value(5));
    }

    // @PreAuthorize("hasRole('CUSTOMER')")가 비로그인 요청을 403 + A004로 거부하는지 검증
    // - SecurityConfig의 PERMIT_ALL_PATHS에 "/api/reviews/**"가 있어도 URL 필터 레벨의 permitAll일 뿐,
    //   메서드 레벨 @PreAuthorize는 별도로 평가되어 여전히 막힌다는 것을 증명하는 테스트(BookControllerTest와 동일 패턴)
    @Test
    @WithAnonymousUser
    void create_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_REQUEST_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("A004"));
    }

    // 관리자 계정도 CUSTOMER 권한이 없으므로 403 + A004로 거부되는지 검증
    @Test
    @WithMockUser(roles = "ADMIN")
    void create_asAdmin_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_REQUEST_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("A004"));
    }

    // score가 유효 범위(1~5)를 벗어나면 @Valid 검증에서 걸려 서비스까지 가지 않고 400 + C001로 응답하는지 검증
    @Test
    @WithMockUser(username = "customer1", roles = "CUSTOMER")
    void create_invalidScore_returnsInvalidInputError() throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"purchaseItemId":1,"score":0,"content":"내용","type":"TEXT"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C001"));
    }

    // 본인이 구매하지 않은 purchaseItemId면 서비스가 던지는 BusinessException(ENTITY_NOT_FOUND)이
    // GlobalExceptionHandler를 거쳐 404 + C004로 응답되는지 검증(소유자 검증 결과가 API 레벨까지 이어지는지)
    @Test
    @WithMockUser(username = "customer1", roles = "CUSTOMER")
    void create_purchaseItemNotOwned_returnsNotFound() throws Exception {
        given(reviewCommandService.createReview(eq("customer1"), any(ReviewCreateRequest.class)))
                .willThrow(new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_REQUEST_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C004"));
    }

    // 본인 리뷰면 수정이 성공하는지 검증
    @Test
    @WithMockUser(username = "customer1", roles = "CUSTOMER")
    void update_owner_returnsUpdatedReview() throws Exception {
        given(reviewCommandService.updateReview(eq("customer1"), eq(1L), any(ReviewUpdateRequest.class)))
                .willReturn(reviewResponse(4, "괜찮아요"));

        mockMvc.perform(put("/api/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_REQUEST_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("괜찮아요"));
    }

    // 타인 리뷰를 수정하려 하면 서비스가 던지는 ENTITY_NOT_FOUND가 404 + C004로 이어지는지 검증
    @Test
    @WithMockUser(username = "stranger", roles = "CUSTOMER")
    void update_notOwner_returnsNotFound() throws Exception {
        given(reviewCommandService.updateReview(eq("stranger"), eq(1L), any(ReviewUpdateRequest.class)))
                .willThrow(new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        mockMvc.perform(put("/api/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_REQUEST_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C004"));
    }

    // 비로그인 요청은 수정도 403 + A004로 거부되는지 검증
    @Test
    @WithAnonymousUser
    void update_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(put("/api/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_REQUEST_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("A004"));
    }

    // 본인 리뷰면 삭제가 성공하는지 검증
    @Test
    @WithMockUser(username = "customer1", roles = "CUSTOMER")
    void delete_owner_returnsSuccess() throws Exception {
        mockMvc.perform(delete("/api/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // 타인 리뷰를 삭제하려 하면 서비스가 던지는 ENTITY_NOT_FOUND가 404 + C004로 이어지는지 검증
    @Test
    @WithMockUser(username = "stranger", roles = "CUSTOMER")
    void delete_notOwner_returnsNotFound() throws Exception {
        willThrow(new BusinessException(ErrorCode.ENTITY_NOT_FOUND))
                .given(reviewCommandService).deleteReview("stranger", 1L);

        mockMvc.perform(delete("/api/reviews/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C004"));
    }

    // 비로그인 요청은 삭제도 403 + A004로 거부되는지 검증
    @Test
    @WithAnonymousUser
    void delete_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/reviews/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("A004"));
    }

    // 고객으로 인증된 요청이면 presigned URL 발급이 성공하는지 검증
    @Test
    @WithMockUser(username = "customer1", roles = "CUSTOMER")
    void issuePresignedUploadUrl_asCustomer_returnsPresignedUploadResponse() throws Exception {
        given(reviewImageService.issuePresignedUploadUrl("photo.jpg"))
                .willReturn(new PresignedUploadResponse("http://localhost:9000/review-images/reviews/abc.jpg",
                        "reviews/abc.jpg"));

        mockMvc.perform(post("/api/reviews/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fileName":"photo.jpg"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.objectKey").value("reviews/abc.jpg"));
    }

    // 비로그인 요청은 presigned URL 발급도 403 + A004로 거부되는지 검증
    @Test
    @WithAnonymousUser
    void issuePresignedUploadUrl_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/reviews/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fileName":"photo.jpg"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("A004"));
    }

    // 이미지 확장자가 아니면(예: .exe) @Valid 검증에서 걸려 서비스까지 가지 않고 400 + C001로 응답하는지 검증
    @Test
    @WithMockUser(username = "customer1", roles = "CUSTOMER")
    void issuePresignedUploadUrl_invalidExtension_returnsInvalidInputError() throws Exception {
        mockMvc.perform(post("/api/reviews/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fileName":"malware.exe"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C001"));
    }
}
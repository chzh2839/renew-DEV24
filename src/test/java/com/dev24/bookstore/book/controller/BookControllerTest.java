package com.dev24.bookstore.book.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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
import com.dev24.bookstore.book.controller.request.BookUpdateRequest;
import com.dev24.bookstore.book.controller.response.BookResponse;
import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.book.repository.BookSearchCondition;
import com.dev24.bookstore.book.service.BookCommandService;
import com.dev24.bookstore.book.service.BookQueryService;
import com.dev24.bookstore.book.service.BookSearchResult;
import com.dev24.bookstore.common.config.SecurityConfig;
import com.dev24.bookstore.common.exception.BusinessException;
import com.dev24.bookstore.common.exception.ErrorCode;

// @WebMvcTest는 @Component인 JwtAuthenticationFilter(Filter 구현체)도 자동 스캔하므로,
// addFilters=false로 실제 필터 체인은 안 타지만 빈 생성 자체를 위해 목 처리가 필요하다 (AuthControllerTest와 동일 패턴).
// SecurityConfig를 명시적으로 Import해야 @EnableMethodSecurity(@PreAuthorize 인터셉터)가 이 슬라이스 컨텍스트에도 등록된다.
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookQueryService bookService;
    @MockitoBean
    private BookCommandService bookCommandService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private Book book(String isbn, String title) {
        return new Book(isbn, title, "저자", "출판사", null, 10000, "내용", null, "프로그래밍", BookStatus.ACTIVE);
    }

    // 검색 파라미터(keyword/category/status) + 페이징 파라미터가 올바르게 바인딩되어 서비스로 전달되고,
    // 응답이 ApiResponse<Page<BookResponse>> 형태로 내려오는지 검증
    @Test
    void search_returnsPagedBooks() throws Exception {
        BookSearchResult result = new BookSearchResult(
                List.of(BookResponse.from(book("9788983920774", "자바의 정석"))), 1);
        given(bookService.search(any(BookSearchCondition.class), any())).willReturn(result);

        mockMvc.perform(get("/api/books")
                        .param("keyword", "자바")
                        .param("category", "프로그래밍")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("자바의 정석"))
                .andExpect(jsonPath("$.data.content[0].isbn").value("9788983920774"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    // status 파라미터가 BookStatus enum으로 변환 불가능한 값이면 기존 ErrorCode.INVALID_TYPE_VALUE(C002)로 응답하는지 검증
    @Test
    void search_invalidStatusValue_returnsInvalidTypeValueError() throws Exception {
        mockMvc.perform(get("/api/books").param("status", "NOT_A_STATUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C002"));
    }

    // 존재하는 id로 상세 조회 시 BookResponse가 그대로 내려오는지 검증
    @Test
    void detail_existingId_returnsBook() throws Exception {
        given(bookService.getDetail(1L)).willReturn(BookResponse.from(book("9788983920774", "자바의 정석")));

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("자바의 정석"));
    }

    // 존재하지 않는 id면 서비스가 던지는 BusinessException(ENTITY_NOT_FOUND)이 GlobalExceptionHandler를 거쳐
    // 404 + C004로 응답되는지 검증
    @Test
    void detail_missingId_returnsNotFound() throws Exception {
        given(bookService.getDetail(999L)).willThrow(new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C004"));
    }

    private static final String UPDATE_REQUEST_JSON = """
            {"title":"자바의 정석 3판","authors":"남궁성","publisher":"도우출판","publishedAt":"2024-01-01",
             "price":20000,"contents":"개정 내용","authorInfo":"저자 소개","category":"IT전문서","status":"ACTIVE"}
            """;

    // 관리자 권한이면 수정이 성공하고 갱신된 BookResponse가 내려오는지 검증
    @Test
    @WithMockUser(roles = "ADMIN")
    void update_asAdmin_updatesBook() throws Exception {
        given(bookCommandService.update(eq(1L), any(BookUpdateRequest.class)))
                .willReturn(BookResponse.from(book("9788983920774", "자바의 정석 3판")));

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_REQUEST_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("자바의 정석 3판"));
    }

    // @PreAuthorize("hasRole('ADMIN')")가 관리자가 아닌 요청을 403 + A004로 거부하는지 검증
    // (AuthControllerTest의 logout_unauthenticated_returnsForbidden과 동일 패턴 - @WithAnonymousUser로 익명 인증 상태 재현)
    @Test
    @WithAnonymousUser
    void update_asNonAdmin_returnsForbidden() throws Exception {
        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_REQUEST_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("A004"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_missingId_returnsNotFound() throws Exception {
        given(bookCommandService.update(eq(999L), any(BookUpdateRequest.class)))
                .willThrow(new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        mockMvc.perform(put("/api/books/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_REQUEST_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C004"));
    }

    // title이 비어있으면 @Valid 검증에서 걸려 서비스까지 가지 않고 400 + C001로 응답하는지 검증
    @Test
    @WithMockUser(roles = "ADMIN")
    void update_blankTitle_returnsInvalidInputError() throws Exception {
        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"","status":"ACTIVE","price":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C001"));
    }
}

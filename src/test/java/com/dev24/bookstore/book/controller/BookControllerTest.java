package com.dev24.bookstore.book.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.dev24.bookstore.auth.security.JwtAuthenticationFilter;
import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.book.repository.BookSearchCondition;
import com.dev24.bookstore.book.service.BookQueryService;

// @WebMvcTest는 @Component인 JwtAuthenticationFilter(Filter 구현체)도 자동 스캔하므로,
// addFilters=false로 실제 필터 체인은 안 타지만 빈 생성 자체를 위해 목 처리가 필요하다 (AuthControllerTest와 동일 패턴).
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookQueryService bookService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private Book book(String isbn, String title) {
        return new Book(isbn, title, "저자", "출판사", null, 10000, "내용", null, "프로그래밍", BookStatus.ACTIVE);
    }

    // 검색 파라미터(keyword/category/status) + 페이징 파라미터가 올바르게 바인딩되어 서비스로 전달되고,
    // 응답이 ApiResponse<Page<BookResponse>> 형태로 내려오는지 검증
    @Test
    void search_returnsPagedBooks() throws Exception {
        Page<Book> page = new PageImpl<>(List.of(book("9788983920774", "자바의 정석")), PageRequest.of(0, 20), 1);
        given(bookService.search(any(BookSearchCondition.class), any())).willReturn(page);

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
}

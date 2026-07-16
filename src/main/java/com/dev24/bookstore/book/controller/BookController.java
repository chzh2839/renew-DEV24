package com.dev24.bookstore.book.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dev24.bookstore.book.controller.request.BookUpdateRequest;
import com.dev24.bookstore.book.controller.response.BookResponse;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.book.repository.BookSearchCondition;
import com.dev24.bookstore.book.service.BookCommandService;
import com.dev24.bookstore.book.service.BookQueryService;
import com.dev24.bookstore.book.service.BookSearchResult;
import com.dev24.bookstore.common.response.ApiResponse;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "도서", description = "도서 검색/상세 조회/수정")
public class BookController {

    private final BookQueryService bookService;
    private final BookCommandService bookCommandService;

    // 동적 검색/필터(keyword/category/status) + Pageable 표준 페이징 - 레거시 rownum 페이징 대체.
    // BookQueryService.search()는 @Cacheable 캐시 값으로 Page가 아닌 BookSearchResult를 반환하므로
    // (Page/PageImpl은 Redis JSON 캐싱에 부적합), 여기서 요청의 Pageable과 합쳐 Page<BookResponse>로 재구성.
    @Operation(summary = "도서 검색", description = "동적 검색/필터(keyword/category/status) + Pageable 표준 페이징")
    @GetMapping
    public ApiResponse<Page<BookResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BookStatus status,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        BookSearchResult result = bookService.search(new BookSearchCondition(keyword, category, status), pageable);
        Page<BookResponse> books = new PageImpl<>(result.content(), pageable, result.totalElements());
        return ApiResponse.success(books);
    }

    // 도서 상세 조회
    @Operation(summary = "도서 상세 조회")
    @GetMapping("/{id}")
    public ApiResponse<BookResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(bookService.getDetail(id));
    }

    // 도서 수정 - 관리자 전용. BookCommandService.update()가 캐시(bookDetail/bookSearch) 무효화까지 담당.
    @Operation(summary = "도서 수정", description = "관리자 전용. 캐시(bookDetail/bookSearch) 무효화까지 함께 처리")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BookResponse> update(@PathVariable Long id, @Valid @RequestBody BookUpdateRequest request) {
        return ApiResponse.success(bookCommandService.update(id, request));
    }
}

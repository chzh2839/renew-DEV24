package com.dev24.bookstore.book.service;

import java.util.List;

import com.dev24.bookstore.book.controller.response.BookResponse;

// BookQueryService.search()의 @Cacheable 캐시 "값"으로 쓰이는 경량 DTO.
public record BookSearchResult(List<BookResponse> content, long totalElements) {
}

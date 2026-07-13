package com.dev24.bookstore.book.repository;

import com.dev24.bookstore.book.domain.BookStatus;

// keyword는 title/authors/publisher/contents 중 하나라도 대소문자 무시 포함(containsIgnoreCase) 매칭.
// 각 필드는 null이면 해당 조건을 적용하지 않는다.
public record BookSearchCondition(String keyword, String category, BookStatus status) {
}

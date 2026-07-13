package com.dev24.bookstore.book.domain;

public enum BookStatus {
    ACTIVE, // 정상판매
    UNREGISTERED, // 미등록
    OUT_OF_PRINT, // 절판
    SOLD_OUT // 품절
}

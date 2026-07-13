package com.dev24.bookstore.book.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "book")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "isbn", nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "authors", length = 100)
    private String authors;

    @Column(name = "publisher", length = 100)
    private String publisher;

    @Column(name = "published_at")
    private LocalDate publishedAt;

    @Column(name = "price")
    private int price;

    // @Lob을 쓰면 Postgres에서 oid(large object)로 매핑되어 Flyway가 만든 TEXT 컬럼과 스키마 검증이 어긋난다.
    // 일반 String 필드로 두면 TEXT/VARCHAR 계열로 매핑되어 일치한다.
    @Column(name = "contents")
    private String contents;

    @Column(name = "author_info")
    private String authorInfo;

    @Column(name = "category", length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookStatus status;

    public Book(String isbn, String title, String authors, String publisher, LocalDate publishedAt,
                int price, String contents, String authorInfo, String category, BookStatus status) {
        this.isbn = isbn;
        this.title = title;
        this.authors = authors;
        this.publisher = publisher;
        this.publishedAt = publishedAt;
        this.price = price;
        this.contents = contents;
        this.authorInfo = authorInfo;
        this.category = category;
        this.status = status;
    }
}

package com.dev24.bookstore.book.controller.response;

import java.time.LocalDate;

import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookImage;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.book.domain.Rating;

public record BookResponse(
        Long id,
        String isbn,
        String title,
        String authors,
        String publisher,
        LocalDate publishedAt,
        int price,
        String category,
        BookStatus status,
        String imageUrl,
        double averageRating,
        int ratingCount,
        int salesCount) {

    // BookImage/Rating은 Book과 별도의 INSERT로 생성되고 모든 Book이 반드시 이미지/평점을 갖는다는 제약이 없으므로,
    // 정상 저장된 Book이라도 book.getBookImage()/getRating()이 null일 수 있다.
    public static BookResponse from(Book book) {
        BookImage bookImage = book.getBookImage();
        Rating rating = book.getRating();

        String imageUrl = bookImage != null ? bookImage.getImageUrl() : null;
        int ratingCount = rating != null ? rating.getRatingCount() : 0;
        int ratingSum = rating != null ? rating.getRatingSum() : 0;
        int salesCount = rating != null ? rating.getSalesCount() : 0;
        double averageRating = ratingCount == 0 ? 0.0 : (double) ratingSum / ratingCount;

        return new BookResponse(
                book.getId(), book.getIsbn(), book.getTitle(), book.getAuthors(), book.getPublisher(),
                book.getPublishedAt(), book.getPrice(), book.getCategory(), book.getStatus(),
                imageUrl, averageRating, ratingCount, salesCount);
    }
}

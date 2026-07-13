package com.dev24.bookstore.book.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.dev24.bookstore.book.config.BookCacheConfig;
import com.dev24.bookstore.book.controller.response.BookResponse;
import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.repository.BookRepository;
import com.dev24.bookstore.book.repository.BookSearchCondition;
import com.dev24.bookstore.common.exception.BusinessException;
import com.dev24.bookstore.common.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookQueryService {

    private final BookRepository bookRepository;

    // 캐시 키: BookSearchCondition(record)과 Pageable(PageRequest) 둘 다 내용 기반 equals()/hashCode()/toString()을
    // 가지므로 커스텀 key SpEL 없이 기본 SimpleKeyGenerator로 충분하다.
    @Cacheable(cacheNames = BookCacheConfig.BOOK_SEARCH_CACHE)
    public BookSearchResult search(BookSearchCondition condition, Pageable pageable) {
        Page<Book> books = bookRepository.search(condition, pageable);
        return new BookSearchResult(books.map(BookResponse::from).getContent(), books.getTotalElements());
    }

    // 존재하지 않는 id는 Optional/null이 아니라 예외로 던진다 - @Cacheable은 메서드가 정상 반환할 때만 캐시에 쓰고
    // 예외가 전파되면 캐시에 아무것도 남기지 않으므로, "이 id는 없다"는 사실 자체가 캐시되어 남는 문제가 생기지 않는다.
    @Cacheable(cacheNames = BookCacheConfig.BOOK_DETAIL_CACHE)
    public BookResponse getDetail(Long id) {
        Book book = bookRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        return BookResponse.from(book);
    }
}

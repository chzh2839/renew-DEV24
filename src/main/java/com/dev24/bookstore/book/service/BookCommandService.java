package com.dev24.bookstore.book.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dev24.bookstore.book.config.BookCacheConfig;
import com.dev24.bookstore.book.controller.request.BookUpdateRequest;
import com.dev24.bookstore.book.controller.response.BookResponse;
import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.repository.BookRepository;
import com.dev24.bookstore.common.exception.BusinessException;
import com.dev24.bookstore.common.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookCommandService {

    private final BookRepository bookRepository;

    // bookDetail은 getDetail(Long id)와 동일한 기본 키(단일 id 파라미터)를 명시해 같은 엔트리를 무효화.
    // bookSearch는 검색조건+페이징 조합마다 키가 달라 특정 엔트리만 지울 수 없어 allEntries로 전체 무효화.
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = BookCacheConfig.BOOK_DETAIL_CACHE, key = "#id"),
            @CacheEvict(cacheNames = BookCacheConfig.BOOK_SEARCH_CACHE, allEntries = true)
    })
    public BookResponse update(Long id, BookUpdateRequest request) {
        // book은 findByIdWithDetails로 조회한 영속 상태 엔티티라, @Transactional 커밋 시점에
        // 더티체킹으로 자동 반영됨 - 별도 save() 호출 불필요
        Book book = bookRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        book.update(request.title(), request.authors(), request.publisher(), request.publishedAt(),
                request.price(), request.contents(), request.authorInfo(), request.category(), request.status());
        return BookResponse.from(book);
    }
}

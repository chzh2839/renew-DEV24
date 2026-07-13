package com.dev24.bookstore.book.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.dev24.bookstore.book.client.KakaoBookClient;
import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookImage;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.book.domain.Rating;
import com.dev24.bookstore.book.repository.BookImageRepository;
import com.dev24.bookstore.book.repository.BookRepository;
import com.dev24.bookstore.book.repository.RatingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 카카오 도서 검색 API로 카탈로그를 1회성으로 시딩한다(라이브 프록시 X).
@Slf4j
@Service
@RequiredArgsConstructor
public class BookSeedService {

    // 호출 횟수를 최소화하기 위해 키워드 수를 적게 유지한다 (검색어당 1회 최대 50건 = 최대 300권).
    private static final List<String> SEED_KEYWORDS =
            List.of("소설", "에세이", "자기계발", "경제경영", "프로그래밍", "인문학");

    private final KakaoBookClient kakaoBookClient;
    private final BookRepository bookRepository;
    private final BookImageRepository bookImageRepository;
    private final RatingRepository ratingRepository;

    public void seed() {
        if (bookRepository.count() > 0) {
            log.info("도서 카탈로그가 이미 시딩되어 있어 건너뜁니다.");
            return;
        }

        int inserted = 0;
        for (String keyword : SEED_KEYWORDS) {
            for (KakaoBookClient.Document document : kakaoBookClient.search(keyword)) {
                if (saveIfNew(document, keyword)) {
                    inserted++;
                }
            }
        }
        log.info("도서 카탈로그 시딩 완료 - {}건 적재", inserted);
    }

    // app 컨테이너가 2개 복제되어 있어(docker-compose) 최초 기동 시 두 인스턴스가 동시에 시딩을 시도할 수 있다.
    // existsByIsbn만으로는 두 인스턴스 사이의 경합을 완전히 막지 못하므로, book.isbn UNIQUE 제약 위반은
    // 정상적인 상황으로 보고 건너뛴다(분산 락 없이도 최종 데이터 정합성은 DB 제약이 보장).
    private boolean saveIfNew(KakaoBookClient.Document document, String keyword) {
        String isbn = extractIsbn13(document.isbn());
        if (isbn == null || bookRepository.existsByIsbn(isbn)) {
            return false;
        }

        try {
            Book book = new Book(
                    isbn,
                    document.title(),
                    String.join(", ", document.authors()),
                    document.publisher(),
                    parsePublishedAt(document.datetime()),
                    document.price(),
                    document.contents(),
                    null, // authorInfo - 카카오 도서 검색 API는 저자 소개를 제공하지 않음
                    keyword,
                    BookStatus.ACTIVE);
            bookRepository.save(book);
            bookImageRepository.save(new BookImage(book, document.thumbnail()));
            ratingRepository.save(new Rating(book));
            return true;
        } catch (DataIntegrityViolationException e) {
            log.debug("isbn={} 은 다른 인스턴스가 먼저 적재해 건너뜁니다.", isbn);
            return false;
        }
    }

    // 카카오 isbn 필드는 "ISBN10 ISBN13"이 공백으로 같이 오거나 하나만 오기도 한다. 13자리 쪽을 우선한다.
    private String extractIsbn13(String rawIsbn) {
        if (rawIsbn == null || rawIsbn.isBlank()) {
            return null;
        }
        String[] tokens = rawIsbn.trim().split("\\s+");
        for (String token : tokens) {
            if (token.length() == 13) {
                return token;
            }
        }
        return tokens[tokens.length - 1];
    }

    private LocalDate parsePublishedAt(String datetime) {
        try {
            return OffsetDateTime.parse(datetime).toLocalDate();
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }
}

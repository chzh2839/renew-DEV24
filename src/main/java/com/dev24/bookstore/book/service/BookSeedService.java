package com.dev24.bookstore.book.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dev24.bookstore.auth.domain.Admin;
import com.dev24.bookstore.auth.repository.AdminRepository;
import com.dev24.bookstore.book.client.KakaoBookClient;
import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookImage;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.book.domain.Rating;
import com.dev24.bookstore.book.repository.BookImageRepository;
import com.dev24.bookstore.book.repository.BookRepository;
import com.dev24.bookstore.book.repository.RatingRepository;
import com.dev24.bookstore.purchase.domain.Stock;
import com.dev24.bookstore.purchase.repository.StockRepository;

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

    // 관리자 등록 UI/API가 없어(범위 밖) 시딩된 책의 Stock.admin에 붙일 관리자가 실제로는 존재하지 않는다.
    // 그래서 시딩 전용 시스템 관리자를 이 loginId로 스스로 확보한다 - 로그인할 일이 없는 계정이라
    // 비밀번호는 알아낼 수 없는 무작위 값을 해싱해서 채운다.
    private static final String SEED_ADMIN_LOGIN_ID = "book-seed-admin";
    // 카카오 응답엔 재고 개념이 없어 시딩 전용 기본값으로 고정한다.
    private static final int DEFAULT_STOCK_QUANTITY = 100;
    private static final int DEFAULT_SAFETY_STOCK = 10;

    private final KakaoBookClient kakaoBookClient;
    private final BookRepository bookRepository;
    private final BookImageRepository bookImageRepository;
    private final RatingRepository ratingRepository;
    private final AdminRepository adminRepository;
    private final StockRepository stockRepository;
    private final PasswordEncoder passwordEncoder;

    public void seed() {
        if (bookRepository.count() > 0) {
            log.info("도서 카탈로그가 이미 시딩되어 있어 건너뜁니다.");
            return;
        }

        Admin seedAdmin = resolveSeedAdmin();
        int inserted = 0;
        for (String keyword : SEED_KEYWORDS) {
            for (KakaoBookClient.Document document : kakaoBookClient.search(keyword)) {
                if (saveIfNew(document, keyword, seedAdmin)) {
                    inserted++;
                }
            }
        }
        log.info("도서 카탈로그 시딩 완료 - {}건 적재", inserted);
    }

    // app 컨테이너가 2개 복제되어 있어(docker-compose) 최초 기동 시 두 인스턴스가 동시에 시딩을 시도할 수 있다.
    // existsByIsbn만으로는 두 인스턴스 사이의 경합을 완전히 막지 못하므로, book.isbn UNIQUE 제약 위반은
    // 정상적인 상황으로 보고 건너뛴다(분산 락 없이도 최종 데이터 정합성은 DB 제약이 보장).
    private boolean saveIfNew(KakaoBookClient.Document document, String keyword, Admin seedAdmin) {
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
            // 카카오 sale_price가 없거나(할인 없음) 0 이하로 오면 정가(price)로 대체한다.
            int salePrice = document.salePrice() > 0 ? document.salePrice() : document.price();
            stockRepository.save(new Stock(book, seedAdmin, DEFAULT_STOCK_QUANTITY, salePrice, DEFAULT_SAFETY_STOCK));
            return true;
        } catch (DataIntegrityViolationException e) {
            log.debug("isbn={} 은 다른 인스턴스가 먼저 적재해 건너뜁니다.", isbn);
            return false;
        }
    }

    private Admin resolveSeedAdmin() {
        return adminRepository.findByLoginId(SEED_ADMIN_LOGIN_ID).orElseGet(this::createSeedAdmin);
    }

    // book.isbn 경합과 동일한 원칙 - 두 인스턴스가 동시에 최초 생성을 시도하면 login_id UNIQUE 위반이 나는
    // 쪽은 예외를 삼키고 먼저 만든 인스턴스의 결과를 다시 조회해서 쓴다.
    private Admin createSeedAdmin() {
        try {
            return adminRepository.save(new Admin(
                    SEED_ADMIN_LOGIN_ID, passwordEncoder.encode(UUID.randomUUID().toString()), "도서 시딩 시스템 계정"));
        } catch (DataIntegrityViolationException e) {
            return adminRepository.findByLoginId(SEED_ADMIN_LOGIN_ID).orElseThrow(() -> e);
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

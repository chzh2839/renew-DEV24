package com.dev24.bookstore.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.dev24.bookstore.book.client.KakaoBookClient;
import com.dev24.bookstore.book.domain.Book;
import com.dev24.bookstore.book.domain.BookStatus;
import com.dev24.bookstore.book.repository.BookImageRepository;
import com.dev24.bookstore.book.repository.BookRepository;
import com.dev24.bookstore.book.repository.RatingRepository;

@ExtendWith(MockitoExtension.class)
class BookSeedServiceTest {

    @Mock
    private KakaoBookClient kakaoBookClient;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookImageRepository bookImageRepository;
    @Mock
    private RatingRepository ratingRepository;

    private BookSeedService bookSeedService;

    @BeforeEach
    void setUp() {
        bookSeedService = new BookSeedService(kakaoBookClient, bookRepository, bookImageRepository, ratingRepository);
    }

    // 이미 카탈로그에 도서가 있으면 카카오 API를 아예 호출하지 않고 건너뛰는지 검증
    @Test
    void seed_alreadySeeded_skipsWithoutCallingApi() {
        given(bookRepository.count()).willReturn(1L);

        bookSeedService.seed();

        verify(kakaoBookClient, never()).search(anyString());
    }

    // 카카오 응답 도큐먼트가 Book/BookImage/Rating으로 올바르게 매핑되어 저장되는지 검증
    // (isbn10+isbn13 혼합 문자열에서 13자리를 우선 선택, 검색어가 category로, 저자 배열이 콤마 join으로)
    @Test
    void seed_mapsDocumentFieldsCorrectly() {
        given(bookRepository.count()).willReturn(0L);
        KakaoBookClient.Document document = new KakaoBookClient.Document(
                "Do it! 점프 투 파이썬", "파이썬 입문서", "8983920775 9788983920774",
                "2019-06-20T00:00:00.000+09:00", List.of("박응용", "홍길동"), "이지스퍼블리싱",
                16920, "http://example.com/cover.jpg");
        given(kakaoBookClient.search("소설")).willReturn(List.of(document));
        given(kakaoBookClient.search("에세이")).willReturn(List.of());
        given(kakaoBookClient.search("자기계발")).willReturn(List.of());
        given(kakaoBookClient.search("경제경영")).willReturn(List.of());
        given(kakaoBookClient.search("프로그래밍")).willReturn(List.of());
        given(kakaoBookClient.search("인문학")).willReturn(List.of());
        given(bookRepository.existsByIsbn("9788983920774")).willReturn(false);
        given(bookRepository.save(any(Book.class))).willAnswer(invocation -> invocation.getArgument(0));

        bookSeedService.seed();

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        Book saved = captor.getValue();
        assertThat(saved.getIsbn()).isEqualTo("9788983920774");
        assertThat(saved.getTitle()).isEqualTo("Do it! 점프 투 파이썬");
        assertThat(saved.getAuthors()).isEqualTo("박응용, 홍길동");
        assertThat(saved.getPublisher()).isEqualTo("이지스퍼블리싱");
        assertThat(saved.getCategory()).isEqualTo("소설");
        assertThat(saved.getStatus()).isEqualTo(BookStatus.ACTIVE);
        verify(bookImageRepository).save(any());
        verify(ratingRepository).save(any());
    }

    // isbn이 이미 존재하는 도서는 저장을 건너뛰는지 검증
    @Test
    void seed_existingIsbn_skipsSave() {
        given(bookRepository.count()).willReturn(0L);
        KakaoBookClient.Document document = new KakaoBookClient.Document(
                "제목", "내용", "9788983920774", "2019-06-20T00:00:00.000+09:00",
                List.of("저자"), "출판사", 10000, "http://example.com/cover.jpg");
        given(kakaoBookClient.search(anyString())).willReturn(List.of(document));
        given(bookRepository.existsByIsbn("9788983920774")).willReturn(true);

        bookSeedService.seed();

        verify(bookRepository, never()).save(any());
    }

    // isbn이 비어있는 도큐먼트는 저장을 건너뛰는지 검증
    @Test
    void seed_blankIsbn_skipsSave() {
        given(bookRepository.count()).willReturn(0L);
        KakaoBookClient.Document document = new KakaoBookClient.Document(
                "제목", "내용", "", "2019-06-20T00:00:00.000+09:00",
                List.of("저자"), "출판사", 10000, "http://example.com/cover.jpg");
        given(kakaoBookClient.search(anyString())).willReturn(List.of(document));

        bookSeedService.seed();

        verify(bookRepository, never()).save(any());
        verify(bookRepository, never()).existsByIsbn(anyString());
    }

    // app 인스턴스 2개가 동시에 시딩을 시도해 같은 isbn으로 저장 경합이 나도(DB unique 제약 위반),
    // 예외를 삼키고 나머지 시딩을 계속 진행하는지 검증
    @Test
    void seed_duplicateKeyRaceOnSave_doesNotAbortRemainingSeeding() {
        given(bookRepository.count()).willReturn(0L);
        KakaoBookClient.Document document = new KakaoBookClient.Document(
                "제목", "내용", "9788983920774", "2019-06-20T00:00:00.000+09:00",
                List.of("저자"), "출판사", 10000, "http://example.com/cover.jpg");
        given(kakaoBookClient.search(anyString())).willReturn(List.of(document));
        given(bookRepository.existsByIsbn("9788983920774")).willReturn(false);
        given(bookRepository.save(any(Book.class))).willThrow(new DataIntegrityViolationException("duplicate key"));

        bookSeedService.seed();

        // 6개 키워드 모두 같은 isbn을 시도하지만 매번 예외를 던지고 넘어갈 뿐 seed() 자체는 정상 종료된다
        verify(bookRepository, times(6)).save(any(Book.class));
        verify(bookImageRepository, never()).save(any());
    }
}

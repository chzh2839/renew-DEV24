package com.dev24.bookstore.book.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

// BookSeedServiceTest는 KakaoBookClient를 목킹하므로 search()의 실제 HTTP 호출 로직은 절대 실행되지 않는다.
// 이 테스트는 실제 dapi.kakao.com에 네트워크 호출을 보내 응답 파싱을 검증한다.
// APP_KAKAO_REST_API_KEY 환경변수(실제 카카오 REST API 키)가 없으면 자동으로 skip된다.
// Edit Configurations에서 환경변수 직접 설정해야 한다.
@EnabledIfEnvironmentVariable(named = "APP_KAKAO_REST_API_KEY", matches = ".+")
class KakaoBookClientIntegrationTest {

    private KakaoBookClient kakaoBookClient;

    @BeforeEach
    void setUp() {
        kakaoBookClient = new KakaoBookClient(System.getenv("APP_KAKAO_REST_API_KEY"));
    }

    @Test
    void search_realKakaoApi_returnsNonEmptyResultsWithPopulatedFields() {
        List<KakaoBookClient.Document> documents = kakaoBookClient.search("자바");

        assertThat(documents).isNotEmpty();
        KakaoBookClient.Document first = documents.get(0);
        assertThat(first.title()).isNotBlank();
        assertThat(first.isbn()).isNotBlank();
    }

    @Test
    void search_noMatchingQuery_returnsEmptyList() {
        List<KakaoBookClient.Document> documents =
                kakaoBookClient.search("asdkjqwoieuqwoeiuasdkjqweqwe존재하지않을검색어zxcvqwer");

        assertThat(documents).isEmpty();
    }
}

package com.dev24.bookstore.book.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KakaoBookClient {

    private static final String BASE_URL = "https://dapi.kakao.com";
    // 카카오 도서 검색 API 1회 호출 최대 건수. 시딩용이라 페이지네이션 없이 검색어당 1회 호출로 제한한다.
    private static final int PAGE_SIZE = 50;

    private final RestClient restClient;

    public KakaoBookClient(@Value("${app.kakao.rest-api-key}") String restApiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("Authorization", "KakaoAK " + restApiKey)
                .build();
    }

    public List<Document> search(String query) {
        SearchResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v3/search/book")
                        .queryParam("query", query)
                        .queryParam("size", PAGE_SIZE)
                        .build())
                .retrieve()
                .body(SearchResponse.class);
        return response == null ? List.of() : response.documents();
    }

    record SearchResponse(List<Document> documents) {
    }

    public record Document(
            String title,
            String contents,
            String isbn,
            String datetime,
            List<String> authors,
            String publisher,
            int price,
            String thumbnail) {
    }
}

package com.dev24.bookstore.book.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.dev24.bookstore.book.service.BookSeedService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// app.book-seed.enabled=true일 때만 앱 기동 시 도서 카탈로그 시딩을 시도한다.
// 기본값 false로 두는 이유는 테스트/CI/평범한 로컬 실행에서 매번 실제 외부 API를 호출하지 않게 하기 위함 - docker-compose에서만 켠다.
// 시딩이 실패해도(키 미설정, 네트워크 오류 등) 앱 기동 자체는 막지 않는다 - 카탈로그가 빈 채로 계속 동작한다.
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.book-seed.enabled", havingValue = "true")
public class BookSeedRunner implements ApplicationRunner {

    private final BookSeedService bookSeedService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            bookSeedService.seed();
        } catch (Exception e) {
            log.warn("도서 카탈로그 시딩 실패 - 카탈로그가 빈 상태로 앱을 계속 기동합니다.", e);
        }
    }
}

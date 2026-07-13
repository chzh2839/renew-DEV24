package com.dev24.bookstore.book.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import com.dev24.bookstore.book.controller.response.BookResponse;
import com.dev24.bookstore.book.service.BookSearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

// 도서 목록/상세 조회에 Redis 기반 Spring Cache(@Cacheable)를 적용한다.
@Configuration
@EnableCaching
public class BookCacheConfig {

    public static final String BOOK_SEARCH_CACHE = "bookSearch";
    public static final String BOOK_DETAIL_CACHE = "bookDetail";

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    @Bean
    public RedisCacheManagerBuilderCustomizer bookCacheCustomizer() {
        // GenericJackson2JsonRedisSerializer/Jackson2JsonRedisSerializer가 내부적으로 만드는 기본 ObjectMapper는
        // Spring Boot 자동설정 ObjectMapper와 달리 JavaTimeModule을 자동 등록하지 않는다.
        // - BookResponse.publishedAt (LocalDate) 직렬화를 위해 명시적으로 등록.
        ObjectMapper redisObjectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        // 새 @Cacheable 캐시를 추가하면 반드시 아래에 withCacheConfiguration(...)도 함께 추가할 것.
        return builder -> builder
                .withCacheConfiguration(BOOK_SEARCH_CACHE, cacheConfig(redisObjectMapper, BookSearchResult.class))
                .withCacheConfiguration(BOOK_DETAIL_CACHE, cacheConfig(redisObjectMapper, BookResponse.class));
    }

    // 다형성 타입 힌트 자체를 포기하고 캐시 이름별로 대상 타입을 미리 알고 있는 `Jackson2JsonRedisSerializer<T>(ObjectMapper, Class<T>)`를 사용
    // 자세한 원리(및 생성자에 따른 예외 케이스)는 docs/CACHING.md 3절 참고.
    private <T> RedisCacheConfiguration cacheConfig(ObjectMapper objectMapper, Class<T> type) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL)
                .disableCachingNullValues()
                .serializeValuesWith(SerializationPair.fromSerializer(
                        new Jackson2JsonRedisSerializer<>(objectMapper, type)));
    }
}

package com.dev24.bookstore.common.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

// 포토 리뷰 업로드 후 콘텐츠 검증처럼 최선 노력(best-effort)이면 충분한 백그라운드 작업용 스레드풀.
// NATS(적립금 지급/재고 알림처럼 앱 재시작에도 반드시 재전달돼야 하는 이벤트)와 달리,
// 검증 하나 놓쳐도 업무적으로 치명적이지 않은 작업이라 별도 메시지 브로커 없이 스프링 내장 @Async로 충분하다.
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "reviewImageValidationExecutor")
    public Executor reviewImageValidationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("review-image-validation-");
        executor.initialize();
        return executor;
    }
}

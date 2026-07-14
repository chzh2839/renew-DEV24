package com.dev24.bookstore.purchase.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.Nats;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;

// 구매 모듈의 OrderCompletedEvent(및 향후 LowStockEvent) 발행/구독에 쓰는 NATS JetStream 연결을 준비한다.
// Redis와 달리 Spring Boot 자동설정이 없는 클라이언트라 커넥션/스트림 준비를 직접 빈으로 등록한다.
//
// 주의: 아래 natsConnection 빈은 생성되는 즉시 연결을 시도하고 실패하면 빈 생성 자체가 실패한다
// (Redis처럼 최초 사용 시점까지 연결을 미루는 지연 연결 모드가 아님 - jnats 클라이언트가 그런 모드를 기본 제공하지 않음).
// 그래서 NATS를 쓰지 않는 나머지 모든 테스트/환경까지 이 빈 생성 실패로 덩달아 죽지 않도록,
// app.book-seed.enabled와 동일한 패턴으로 app.nats.enabled 플래그 뒤에 숨긴다.
//  => 기본값 false(application.properties), docker-compose와 NATS 관련 테스트에서만 true로 켠다.
@Configuration
@ConditionalOnProperty(name = "app.nats.enabled", havingValue = "true")
public class NatsConfig {

    private static final String STREAM_NAME = "ORDERS";
    private static final String STREAM_SUBJECTS = "orders.>";

    @Bean(destroyMethod = "close")
    public Connection natsConnection(@Value("${app.nats.url}") String natsUrl) throws IOException, InterruptedException {
        return Nats.connect(natsUrl);
    }

    @Bean
    public JetStream jetStream(Connection natsConnection) throws IOException, JetStreamApiException {
        ensureStreamExists(natsConnection);
        return natsConnection.jetStream();
    }

    // getStreamInfo가 예외를 던지면(스트림이 없으면) addStream으로 생성 - 재기동 시 이미 있으면 그대로 둔다(idempotent)
    private void ensureStreamExists(Connection connection) throws IOException, JetStreamApiException {
        JetStreamManagement jsm = connection.jetStreamManagement();
        try {
            jsm.getStreamInfo(STREAM_NAME);
        } catch (JetStreamApiException e) {
            jsm.addStream(StreamConfiguration.builder()
                    .name(STREAM_NAME)
                    .subjects(STREAM_SUBJECTS)
                    .storageType(StorageType.File)
                    .build());
        }
    }
}

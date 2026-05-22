package com.example.pricing.service.redis;

import com.example.dto.TelemetryEvent;
import com.example.pricing.service.PricingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReactiveStreamConsumer {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final PricingService pricingService;
    private final RedisService redisService;

    @Value("${redis.consumer.topic}")
    private String topic;
    @Value("${redis.consumer.group}")
    private String group;
    @Value("${redis.consumer.name}")
    private String consumerName;

    private Disposable subscription;

    @PostConstruct
    public void startConsuming() {
        Consumer consumer = Consumer.from(group, consumerName);
        StreamOffset<String> offset = StreamOffset.create(topic, ReadOffset.lastConsumed());

        Flux<MapRecord<String, Object, Object>> stream = redisTemplate.opsForStream().read(consumer, offset);

        subscription = stream
                .onBackpressureBuffer(1024)
                .flatMap(this::processMessage)
                .onErrorResume(err -> {
                    log.error("Ошибка в потоке потребления, продолжаем: {}", err.getMessage());
                    return Mono.empty();
                })
                .repeat()
                .retry()
                .subscribe(
                        null,
                        err -> log.error("Фатальная ошибка потребления потока: {}", err.getMessage(), err),
                        () -> log.info("Поток потребления завершён")
                );

        log.info("Реактивный потребитель запущен: stream={}, group={}, consumer={}", topic, group, consumerName);
    }

    Mono<Void> processMessage(MapRecord<String, Object, Object> message) {
        String messageId = message.getId().getValue();
        Map<String, String> body = message.getValue().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        e -> String.valueOf(e.getValue())
                ));

        TelemetryEvent event;
        try {
            event = objectMapper.convertValue(body, TelemetryEvent.class);
        } catch (IllegalArgumentException e) {
            log.error("Не удалось десериализовать сообщение {}: {}", messageId, e.getMessage());
            return redisService.acknowledge(messageId);
        }

        log.info("Получено сообщение {}: deviceId={}, eventType={}", messageId, event.getDeviceId(), event.getEventType());

        return pricingService.calculateNewPrice(messageId, event);
    }

    @PreDestroy
    public void stopConsuming() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            log.info("Реактивный потребитель остановлен");
        }
    }
}

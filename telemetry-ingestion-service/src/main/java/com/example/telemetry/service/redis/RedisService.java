package com.example.telemetry.service.redis;

import com.example.dto.TelemetryEvent;
import com.example.telemetry.service.exception.RedisOperationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
public class RedisService {
    @Value("${redis.producer.topic}")
    private String topic;

    private final ObjectMapper objectMapper;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public RedisService(ObjectMapper objectMapper, ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    public Mono<String> saveData(TelemetryEvent telemetryEvent) {
        Map<String, Object> map = objectMapper.convertValue(telemetryEvent, Map.class);
        Map<String, String> stringMap = map.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        e -> String.valueOf(e.getValue())
                ));
        MapRecord<String, String, String> record = MapRecord.create(topic, stringMap);

        return redisTemplate.opsForStream()
                .add(record)
                .map(RecordId::getValue)
                .doOnError(error -> log.warn("Временная ошибка записи в Redis, повторная попытка..."))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
                .onErrorMap(e -> {
                    log.error("Все попытки записи в Redis исчерпаны: {}", e.getMessage());
                    return new RedisOperationException(e);
                });
    }
}

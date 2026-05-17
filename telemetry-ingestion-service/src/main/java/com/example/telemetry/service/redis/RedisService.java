package com.example.telemetry.service.redis;

import com.example.dto.TelemetryEvent;
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
        Map map = objectMapper.convertValue(telemetryEvent, Map.class);

        MapRecord<String, String, String> record = MapRecord.create(topic, map);

        var result = redisTemplate.opsForStream()
                .add(record)
                .map(RecordId::getValue)
                .doOnError(error -> log.warn("Temporary error writing to Redis, retrying..."))
                // Сделай 3 попытки с увеличивающейся паузой (backoff) стартуя с 100мс
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
                // Если и после 3 попыток не вышло — логируем окончательный фейл
                .doOnError(error -> log.error("All retry attempts failed to write to Redis: {}", error.getMessage()));

        return result;
    }
}
package com.example.telemetry.service.controller;

import com.example.dto.TelemetryEvent;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/telemetry")
public class TelemetryController {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public TelemetryController(ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> process(@RequestBody TelemetryEvent event) {
        // Здесь мы просто возвращаем Mono<Void>
        // WebFlux сам подпишется на него и отправит ответ 202 Accepted
        ObjectRecord<String, TelemetryEvent> record = ObjectRecord.create("telemetry:events", event);

        return redisTemplate.opsForStream()
                .add(record)
                .map(RecordId::getValue)
                .then(); // Превращаем результат (длина списка) в Mono<Void>
    }
}
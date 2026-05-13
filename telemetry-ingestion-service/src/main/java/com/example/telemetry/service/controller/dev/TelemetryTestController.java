package com.example.telemetry.service.controller.dev;

import com.example.dto.TelemetryEvent;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/test", produces = MediaType.APPLICATION_JSON_VALUE)
public class TelemetryTestController {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public TelemetryTestController(ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/send")
    public Mono<String> sendTestEvent() {
        TelemetryEvent event = new TelemetryEvent("device-123", "HIGH_DEMAND", "Hello", 1L);
        ObjectRecord<String, TelemetryEvent> record = ObjectRecord.create("telemetry:events", event);
        // Записываем в стрим
        return redisTemplate.opsForStream()
                .add(record)
                .map(RecordId::getValue);
    }
}
package com.example.pricing.service;

import com.example.dto.TelemetryEvent;
import org.slf4j.Logger;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class TelemetryStreamListener implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(PricingService.class);

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final PricingService pricingService;

    public TelemetryStreamListener(ReactiveRedisTemplate<String, String> redisTemplate, PricingService pricingService) {
        this.redisTemplate = redisTemplate;
        this.pricingService = pricingService;
    }

    // Метод, который будет постоянно слушать поток
    @Override
    public void run(String... args) {
        String streamKey = "telemetry:events";
        redisTemplate.opsForStream()
                .read(TelemetryEvent.class, StreamOffset.fromStart(streamKey))
                .map(ObjectRecord::getValue)
                .repeat() // Читаем бесконечно
                .flatMap(pricingService::calculateNewPrice) // Вызываем логику
                .doOnError(e -> log.error("Error in stream processing", e))
                .subscribe();
    }
}
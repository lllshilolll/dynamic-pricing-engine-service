package com.example.telemetry.service;

import com.example.dto.TelemetryEvent;
import com.example.telemetry.service.redis.RedisService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TelemetryService {

    private final RedisService redisService;

    public TelemetryService(RedisService redisService) {
        this.redisService = redisService;
    }

    public Mono<String> saveTelemetry(TelemetryEvent telemetryEvent) {
        return redisService.saveData(telemetryEvent);
    }
}
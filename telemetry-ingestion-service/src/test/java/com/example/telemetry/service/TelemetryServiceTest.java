package com.example.telemetry.service;

import com.example.dto.TelemetryEvent;
import com.example.telemetry.service.redis.RedisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelemetryServiceTest {

    @Mock
    private RedisService redisService;

    @InjectMocks
    private TelemetryService telemetryService;

    @Test
    void saveTelemetry_ShouldDelegateToRedisService() {
        TelemetryEvent event = new TelemetryEvent("device-1", "HIGH_DEMAND", "test", 1000L);
        when(redisService.saveData(event)).thenReturn(Mono.just("1715712000000-0"));

        Mono<String> result = telemetryService.saveTelemetry(event);

        StepVerifier.create(result)
                .expectNext("1715712000000-0")
                .verifyComplete();

        verify(redisService, times(1)).saveData(event);
    }

    @Test
    void saveTelemetry_WhenRedisFails_ShouldPropagateError() {
        TelemetryEvent event = new TelemetryEvent("device-1", "LOW_STOCK", "test", 1000L);
        when(redisService.saveData(event)).thenReturn(Mono.error(new RuntimeException("Redis error")));

        Mono<String> result = telemetryService.saveTelemetry(event);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e.getMessage().equals("Redis error"))
                .verify();
    }
}

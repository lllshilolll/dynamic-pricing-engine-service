package com.example.telemetry.service.redis;

import com.example.dto.TelemetryEvent;
import com.example.telemetry.service.exception.RedisOperationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStreamOperations;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    @Mock
    private ReactiveRedisTemplate<String, Object> redisTemplate;

    @Mock
    private ReactiveStreamOperations<String, Object, Object> streamOperations;

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private RedisService redisService;

    private TelemetryEvent sampleEvent;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(redisService, "topic", "telemetry:events");
        sampleEvent = new TelemetryEvent();
        sampleEvent.setDeviceId("device-123");
        sampleEvent.setEventType("HIGH_DEMAND");
    }

    @Test
    void saveData_Success_ShouldReturnRecordId() {
        RecordId mockRecordId = RecordId.of("1715712000000-0");

        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
        when(streamOperations.add(any(MapRecord.class))).thenReturn(Mono.just(mockRecordId));

        Mono<String> result = redisService.saveData(sampleEvent);

        StepVerifier.create(result)
                .expectNext("1715712000000-0")
                .verifyComplete();

        verify(streamOperations, times(1)).add(any(MapRecord.class));
    }

    @Test
    void saveData_RedisThrowsError_ShouldMapToRedisOperationException() {
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);

        AtomicInteger actualAttemptsCount = new AtomicInteger(0);

        when(streamOperations.add(any(MapRecord.class))).thenAnswer(invocation ->
                Mono.error(new RuntimeException("Redis temporary error"))
                        .doOnSubscribe(subscription -> actualAttemptsCount.incrementAndGet())
        );

        Mono<String> result = redisService.saveData(sampleEvent);

        StepVerifier.withVirtualTime(() -> result)
                .expectSubscription()
                .thenAwait(java.time.Duration.ofSeconds(5))
                .expectErrorMatches(throwable -> throwable instanceof RedisOperationException)
                .verify();

        Assertions.assertEquals(4, actualAttemptsCount.get(),
                "Количество попыток (1 старт + ретраи) должно быть строго равно 4");

        verify(streamOperations, times(1)).add(any(MapRecord.class));
    }
}

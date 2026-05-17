package com.example.telemetry.service.redis;

import com.example.dto.TelemetryEvent;
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
        // Задаем имя топика через Reflection, так как оно внедряется через @Value
        ReflectionTestUtils.setField(redisService, "topic", "telemetry:events");

        // Инициализируем тестовый объект данных
        sampleEvent = new TelemetryEvent();
        sampleEvent.setDeviceId("device-123");
        sampleEvent.setEventType("HIGH_DEMAND");
    }

    @Test
    void saveData_Success_ShouldReturnRecordId() {
        // Given
        RecordId mockRecordId = RecordId.of("1715712000000-0");

        // Настраиваем цепочку вызовов: redisTemplate.opsForStream() -> streamOperations
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
        when(streamOperations.add(any(MapRecord.class))).thenReturn(Mono.just(mockRecordId));

        // When
        Mono<String> result = redisService.saveData(sampleEvent);


        // Then (Проверка с помощью StepVerifier)
        StepVerifier.create(result)
                .expectNext("1715712000000-0") // Ожидаем конкретную строку RecordId
                .verifyComplete();            // Проверяем, что поток успешно завершился

        verify(streamOperations, times(1)).add(any(MapRecord.class));
    }

    @Test
    void saveData_RedisThrowsError_ShouldPropagateException() {
        // Given
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);

        AtomicInteger actualAttemptsCount = new AtomicInteger(0);

        // Вместо thenReturn используем thenAnswer
        when(streamOperations.add(any(MapRecord.class))).thenAnswer(invocation -> {
            // Каждый раз, когда Reactor будет перезапускать этот Mono,
            // сработает doOnSubscribe, и наш счетчик увеличится
            return Mono.error(new RuntimeException("Redis temporary error"))
                    .doOnSubscribe(subscription -> actualAttemptsCount.incrementAndGet());
        });

        // When
        Mono<String> result = redisService.saveData(sampleEvent);

        // Then
        StepVerifier.withVirtualTime(() -> result)
                .expectSubscription()
                .thenAwait(java.time.Duration.ofSeconds(5)) // Прокручиваем время backoff
                .expectErrorMatches(throwable -> throwable.getClass().getName().contains("RetryExhaustedException"))
                .verify();

        // Самая главная проверка:
        // 1 (первый вызов) + 3 (ретрая) = 4 реальных подписки на поток данных!
        Assertions.assertEquals(4, actualAttemptsCount.get(),
                "Количество попыток (1 старт + ретраи) должно быть строго равно 4");

        // При этом Mockito зафиксирует, что сам конвейер собрался 1 раз
        verify(streamOperations, times(1)).add(any(MapRecord.class));
    }
}

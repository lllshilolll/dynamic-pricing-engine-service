package com.example.pricing.service.redis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    @Mock
    private ReactiveRedisTemplate<String, Object> redisTemplate;

    @SuppressWarnings("unchecked")
    @Mock
    private ReactiveValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisService redisService;

    @Test
    void cachePrice_ShouldCompleteSuccessfully() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.set(any(), any(), any())).thenReturn(Mono.just(true));

        StepVerifier.create(redisService.cachePrice("device-1", 1.5))
                .verifyComplete();

        verify(valueOperations).set(eq("price:device-1"), eq("1.5"), any());
    }

    @Test
    void cachePrice_WhenRedisFails_ShouldNotPropagateError() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.set(any(), any(), any())).thenReturn(Mono.error(new RuntimeException("Redis error")));

        StepVerifier.create(redisService.cachePrice("device-1", 1.5))
                .verifyComplete();
    }

    @Test
    void getCachedPrice_ShouldReturnValue() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("price:device-1")).thenReturn(Mono.just("1.5"));

        StepVerifier.create(redisService.getCachedPrice("device-1"))
                .expectNext("1.5")
                .verifyComplete();
    }
}

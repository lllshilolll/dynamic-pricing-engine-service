package com.example.pricing.service.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStreamOperations;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    @Mock
    private ReactiveRedisTemplate<String, Object> redisTemplate;

    @Mock
    private ReactiveStreamOperations<String, Object, Object> streamOperations;

    @InjectMocks
    private RedisService redisService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(redisService, "topic", "telemetry:events");
        ReflectionTestUtils.setField(redisService, "group", "my-group");
    }

    @Test
    void acknowledge_ShouldCompleteSuccessfully() {
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
        when(streamOperations.acknowledge("telemetry:events", "my-group", "msg-1"))
                .thenReturn(Mono.just(1L));

        StepVerifier.create(redisService.acknowledge("msg-1"))
                .verifyComplete();

        verify(streamOperations).acknowledge("telemetry:events", "my-group", "msg-1");
    }

    @Test
    void acknowledge_WhenRedisFails_ShouldNotPropagateError() {
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
        when(streamOperations.acknowledge("telemetry:events", "my-group", "msg-1"))
                .thenReturn(Mono.error(new RuntimeException("Redis error")));

        StepVerifier.create(redisService.acknowledge("msg-1"))
                .verifyComplete();
    }
}

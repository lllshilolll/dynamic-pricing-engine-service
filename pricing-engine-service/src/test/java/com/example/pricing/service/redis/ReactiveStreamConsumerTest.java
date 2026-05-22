package com.example.pricing.service.redis;

import com.example.dto.TelemetryEvent;
import com.example.pricing.service.PricingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactiveStreamConsumerTest {

    @Mock
    private ReactiveRedisTemplate<String, Object> redisTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PricingService pricingService;

    @Mock
    private RedisService redisService;

    private ReactiveStreamConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new ReactiveStreamConsumer(redisTemplate, objectMapper, pricingService, redisService);
        ReflectionTestUtils.setField(consumer, "topic", "telemetry:events");
        ReflectionTestUtils.setField(consumer, "group", "my-group");
        ReflectionTestUtils.setField(consumer, "consumerName", "consumer-1");
    }

    private Map<Object, Object> bodyOf(String... kv) {
        Map<Object, Object> map = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put(kv[i], kv[i + 1]);
        }
        return map;
    }

    @Test
    void processMessage_ShouldConvertAndDelegate() {
        MapRecord<String, Object, Object> record = MapRecord.create("telemetry:events",
                bodyOf("deviceId", "device-1", "eventType", "HIGH_DEMAND", "payload", "test", "timestamp", "1000"));
        when(pricingService.calculateNewPrice(any(), any(TelemetryEvent.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(consumer.processMessage(record)).verifyComplete();
        verify(pricingService).calculateNewPrice(any(), any(TelemetryEvent.class));
    }

    @Test
    void processMessage_ShouldParseFieldsCorrectly() {
        MapRecord<String, Object, Object> record = MapRecord.create("telemetry:events",
                bodyOf("deviceId", "device-42", "eventType", "LOW_STOCK", "payload", "data", "timestamp", "9999"));
        when(pricingService.calculateNewPrice(any(), any(TelemetryEvent.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(consumer.processMessage(record)).verifyComplete();
        ArgumentCaptor<TelemetryEvent> captor = ArgumentCaptor.forClass(TelemetryEvent.class);
        verify(pricingService).calculateNewPrice(any(), captor.capture());

        TelemetryEvent captured = captor.getValue();
        assertEquals("device-42", captured.getDeviceId());
        assertEquals("LOW_STOCK", captured.getEventType());
        assertEquals("data", captured.getPayload());
        assertEquals(9999L, captured.getTimestamp());
    }

    @Test
    void processMessage_WithEmptyBody_ShouldDelegateToPricingService() {
        MapRecord<String, Object, Object> record = MapRecord.create("telemetry:events", new HashMap<>());
        when(pricingService.calculateNewPrice(any(), any(TelemetryEvent.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(consumer.processMessage(record)).verifyComplete();
        verify(pricingService).calculateNewPrice(any(), any(TelemetryEvent.class));
    }

    @Test
    void processMessage_WhenPricingFails_ShouldPropagateError() {
        MapRecord<String, Object, Object> record = MapRecord.create("telemetry:events",
                bodyOf("deviceId", "device-1", "eventType", "HIGH_DEMAND", "payload", "test", "timestamp", "1000"));
        when(pricingService.calculateNewPrice(any(), any(TelemetryEvent.class)))
                .thenReturn(Mono.error(new RuntimeException("Mongo down")));

        StepVerifier.create(consumer.processMessage(record))
                .expectErrorMatches(e -> e.getMessage().equals("Mongo down"))
                .verify();
    }
}

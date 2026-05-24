package com.example.pricing.service;

import com.example.dto.PriceChangeEvent;
import com.example.dto.TelemetryEvent;
import com.example.pricing.dto.DeviceState;
import com.example.pricing.service.redis.RedisService;
import com.example.pricing.service.repository.DeviceStateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private DeviceStateRepository deviceStateRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private ReactiveKafkaProducerTemplate<String, PriceChangeEvent> priceChangeProducer;

    @InjectMocks
    private PricingService pricingService;

    @Test
    void calculateNewPrice_HighDemand_ShouldSetCoefficient1_5() {
        ReflectionTestUtils.setField(pricingService, "priceChangesTopic", "price.changes");
        TelemetryEvent event = new TelemetryEvent("device-1", "HIGH_DEMAND", "test", 1000L);
        when(deviceStateRepository.findByDeviceId("device-1")).thenReturn(Mono.empty());
        when(deviceStateRepository.save(any(DeviceState.class)))
                .thenReturn(Mono.just(new DeviceState("id1", "device-1", 1.5, "HIGH_DEMAND", null)));
        when(redisService.cachePrice("device-1", 1.5)).thenReturn(Mono.empty());
        when(priceChangeProducer.send(eq("price.changes"), eq("device-1"), any(PriceChangeEvent.class)))
                .thenReturn(Mono.just(mock(SenderResult.class)));

        StepVerifier.create(pricingService.calculateNewPrice(event))
                .verifyComplete();

        verify(redisService).cachePrice("device-1", 1.5);
        verify(priceChangeProducer).send(eq("price.changes"), eq("device-1"), argThat(e ->
                e.getDeviceId().equals("device-1") && e.getPriceBefore() == 1.0 && e.getPriceAfter() == 1.5
        ));
    }

    @Test
    void calculateNewPrice_LowStock_ShouldSetCoefficient1_0() {
        ReflectionTestUtils.setField(pricingService, "priceChangesTopic", "price.changes");
        TelemetryEvent event = new TelemetryEvent("device-2", "LOW_STOCK", "test", 1000L);
        when(deviceStateRepository.findByDeviceId("device-2")).thenReturn(Mono.empty());
        when(deviceStateRepository.save(any(DeviceState.class)))
                .thenReturn(Mono.just(new DeviceState("id2", "device-2", 1.0, "LOW_STOCK", null)));
        when(redisService.cachePrice("device-2", 1.0)).thenReturn(Mono.empty());
        when(priceChangeProducer.send(eq("price.changes"), eq("device-2"), any(PriceChangeEvent.class)))
                .thenReturn(Mono.just(mock(SenderResult.class)));

        StepVerifier.create(pricingService.calculateNewPrice(event))
                .verifyComplete();

        verify(redisService).cachePrice("device-2", 1.0);
    }

    @Test
    void calculateNewPrice_ExistingDevice_ShouldUpdatePrice() {
        ReflectionTestUtils.setField(pricingService, "priceChangesTopic", "price.changes");
        TelemetryEvent event = new TelemetryEvent("device-1", "HIGH_DEMAND", "test", 1000L);
        DeviceState existing = new DeviceState("id1", "device-1", 1.0, "LOW_STOCK", null);
        when(deviceStateRepository.findByDeviceId("device-1")).thenReturn(Mono.just(existing));
        when(deviceStateRepository.save(any(DeviceState.class)))
                .thenReturn(Mono.just(new DeviceState("id1", "device-1", 1.5, "HIGH_DEMAND", null)));
        when(redisService.cachePrice("device-1", 1.5)).thenReturn(Mono.empty());
        when(priceChangeProducer.send(eq("price.changes"), eq("device-1"), any(PriceChangeEvent.class)))
                .thenReturn(Mono.just(mock(SenderResult.class)));

        StepVerifier.create(pricingService.calculateNewPrice(event))
                .verifyComplete();

        verify(redisService).cachePrice("device-1", 1.5);
        verify(priceChangeProducer).send(eq("price.changes"), eq("device-1"), argThat(e ->
                e.getPriceBefore() == 1.0 && e.getPriceAfter() == 1.5
        ));
    }

    @Test
    void calculateNewPrice_SaveFails_ShouldPropagateError() {
        TelemetryEvent event = new TelemetryEvent("device-4", "HIGH_DEMAND", "test", 1000L);
        when(deviceStateRepository.findByDeviceId("device-4")).thenReturn(Mono.empty());
        when(deviceStateRepository.save(any(DeviceState.class)))
                .thenReturn(Mono.error(new RuntimeException("Mongo error")));

        StepVerifier.create(pricingService.calculateNewPrice(event))
                .expectErrorMatches(e -> e.getMessage().equals("Mongo error"))
                .verify();

        verify(redisService, never()).cachePrice(any(), anyDouble());
    }
}

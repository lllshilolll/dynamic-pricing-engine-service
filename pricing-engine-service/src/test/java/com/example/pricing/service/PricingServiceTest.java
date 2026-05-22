package com.example.pricing.service;

import com.example.dto.TelemetryEvent;
import com.example.pricing.dto.PriceUpdate;
import com.example.pricing.service.exception.PersistenceException;
import com.example.pricing.service.redis.RedisService;
import com.example.pricing.service.repository.PricingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private PricingRepository pricingRepository;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private PricingService pricingService;

    @Test
    void calculateNewPrice_HighDemand_ShouldUseCoefficient1_5() {
        TelemetryEvent event = new TelemetryEvent("device-1", "HIGH_DEMAND", "test", 1000L);
        when(pricingRepository.save(any(PriceUpdate.class))).thenReturn(Mono.just(new PriceUpdate("id1", "device-1", 1.5, null)));
        when(redisService.acknowledge("msg-1")).thenReturn(Mono.empty());

        StepVerifier.create(pricingService.calculateNewPrice("msg-1", event))
                .verifyComplete();

        ArgumentCaptor<PriceUpdate> captor = ArgumentCaptor.forClass(PriceUpdate.class);
        verify(pricingRepository).save(captor.capture());
        assertEquals("device-1", captor.getValue().getDeviceId());
        assertEquals(1.5, captor.getValue().getPriceCoefficient());
        verify(redisService).acknowledge("msg-1");
    }

    @Test
    void calculateNewPrice_LowStock_ShouldUseCoefficient1_0() {
        TelemetryEvent event = new TelemetryEvent("device-2", "LOW_STOCK", "test", 1000L);
        when(pricingRepository.save(any(PriceUpdate.class))).thenReturn(Mono.just(new PriceUpdate("id2", "device-2", 1.0, null)));
        when(redisService.acknowledge("msg-2")).thenReturn(Mono.empty());

        StepVerifier.create(pricingService.calculateNewPrice("msg-2", event))
                .verifyComplete();

        ArgumentCaptor<PriceUpdate> captor = ArgumentCaptor.forClass(PriceUpdate.class);
        verify(pricingRepository).save(captor.capture());
        assertEquals(1.0, captor.getValue().getPriceCoefficient());
        verify(redisService).acknowledge("msg-2");
    }

    @Test
    void calculateNewPrice_UnknownEventType_ShouldUseCoefficient1_0() {
        TelemetryEvent event = new TelemetryEvent("device-3", "SOME_NEW_EVENT", "test", 1000L);
        when(pricingRepository.save(any(PriceUpdate.class))).thenReturn(Mono.just(new PriceUpdate("id3", "device-3", 1.0, null)));
        when(redisService.acknowledge("msg-3")).thenReturn(Mono.empty());

        StepVerifier.create(pricingService.calculateNewPrice("msg-3", event))
                .verifyComplete();

        verify(redisService).acknowledge("msg-3");
    }

    @Test
    void calculateNewPrice_SaveFails_ShouldNotAcknowledge() {
        TelemetryEvent event = new TelemetryEvent("device-4", "HIGH_DEMAND", "test", 1000L);
        when(pricingRepository.save(any(PriceUpdate.class))).thenReturn(Mono.error(new RuntimeException("Mongo error")));

        StepVerifier.create(pricingService.calculateNewPrice("msg-4", event))
                .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().equals("Mongo error"))
                .verify();

        verify(redisService, never()).acknowledge(anyString());
    }

    @Test
    void calculateNewPrice_MongoError_ShouldMapToPersistenceException() {
        TelemetryEvent event = new TelemetryEvent("device-5", "HIGH_DEMAND", "test", 1000L);
        com.mongodb.MongoException mongoEx = new com.mongodb.MongoException("write failed");
        when(pricingRepository.save(any(PriceUpdate.class))).thenReturn(Mono.error(mongoEx));

        StepVerifier.create(pricingService.calculateNewPrice("msg-5", event))
                .expectErrorMatches(e -> e instanceof PersistenceException)
                .verify();

        verify(redisService, never()).acknowledge(anyString());
    }
}

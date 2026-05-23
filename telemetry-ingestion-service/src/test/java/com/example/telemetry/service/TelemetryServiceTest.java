package com.example.telemetry.service;

import com.example.dto.TelemetryEvent;
import com.example.telemetry.service.kafka.KafkaProducerService;
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
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private TelemetryService telemetryService;

    @Test
    void saveTelemetry_ShouldDelegateToKafkaProducer() {
        TelemetryEvent event = new TelemetryEvent("device-1", "HIGH_DEMAND", "test", 1000L);
        when(kafkaProducerService.publish(event)).thenReturn(Mono.just("42"));

        Mono<String> result = telemetryService.saveTelemetry(event);

        StepVerifier.create(result)
                .expectNext("42")
                .verifyComplete();

        verify(kafkaProducerService, times(1)).publish(event);
    }

    @Test
    void saveTelemetry_WhenKafkaFails_ShouldPropagateError() {
        TelemetryEvent event = new TelemetryEvent("device-1", "LOW_STOCK", "test", 1000L);
        when(kafkaProducerService.publish(event)).thenReturn(Mono.error(new RuntimeException("Kafka error")));

        Mono<String> result = telemetryService.saveTelemetry(event);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e.getMessage().equals("Kafka error"))
                .verify();
    }
}

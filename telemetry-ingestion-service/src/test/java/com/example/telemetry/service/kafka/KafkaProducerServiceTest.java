package com.example.telemetry.service.kafka;

import com.example.dto.TelemetryEvent;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

    @Mock
    private ReactiveKafkaProducerTemplate<String, TelemetryEvent> producerTemplate;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(kafkaProducerService, "topic", "telemetry.events");
    }

    @SuppressWarnings("unchecked")
    @Test
    void publish_ShouldReturnOffset() {
        TelemetryEvent event = new TelemetryEvent("device-1", "HIGH_DEMAND", "test", 1000L);
        SenderResult<Void> senderResult = mock(SenderResult.class);
        RecordMetadata metadata = new RecordMetadata(new TopicPartition("telemetry.events", 0), 42L, 0, 0L, 0, 0);
        when(senderResult.recordMetadata()).thenReturn(metadata);

        when(producerTemplate.send(eq("telemetry.events"), eq("device-1"), eq(event)))
                .thenReturn(Mono.just(senderResult));

        StepVerifier.create(kafkaProducerService.publish(event))
                .expectNext("42")
                .verifyComplete();

        verify(producerTemplate).send("telemetry.events", "device-1", event);
    }
}

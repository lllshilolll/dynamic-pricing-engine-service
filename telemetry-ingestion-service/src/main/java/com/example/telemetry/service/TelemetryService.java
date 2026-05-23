package com.example.telemetry.service;

import com.example.dto.TelemetryEvent;
import com.example.telemetry.service.kafka.KafkaProducerService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TelemetryService {

    private final KafkaProducerService kafkaProducerService;

    public TelemetryService(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    public Mono<String> saveTelemetry(TelemetryEvent telemetryEvent) {
        return kafkaProducerService.publish(telemetryEvent);
    }
}
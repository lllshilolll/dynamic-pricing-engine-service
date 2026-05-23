package com.example.pricing.service.kafka;

import com.example.dto.TelemetryEvent;
import com.example.pricing.service.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final PricingService pricingService;

    @KafkaListener(topics = "${kafka.topic.telemetry}", groupId = "pricing-group")
    public void consume(TelemetryEvent event) {
        log.info("Получено событие из Kafka: deviceId={}, eventType={}", event.getDeviceId(), event.getEventType());

        try {
            pricingService.calculateNewPrice(event).block();
        } catch (Exception e) {
            log.error("Ошибка обработки для устройства {}: {}", event.getDeviceId(), e.getMessage());
            throw e;
        }
    }
}

package com.example.generator;

import com.example.dto.TelemetryEvent;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class TelemetryGenerator {

    @Value("${generator.device-count:50}")
    private int deviceCount;

    @Value("${generator.events-per-second:10}")
    private int eventsPerSecond;

    @Value("${generator.event-types:HIGH_DEMAND,LOW_STOCK,TEMPERATURE_ALERT,SALES_REPORT,DOOR_OPENED}")
    private String eventTypesConfig;

    @Value("${generator.target-url:http://localhost:8082/api/telemetry}")
    private String targetUrl;

    @Value("${generator.enabled:true}")
    private boolean enabled;

    private final RestTemplate restTemplate;
    private final Random random = new Random();
    private final AtomicLong sentCount = new AtomicLong(0);
    private List<String> eventTypes;

    public TelemetryGenerator(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @PostConstruct
    void init() {
        eventTypes = List.of(eventTypesConfig.split(","));
        log.info("Генератор: {} устройств, {} событий/сек, типы: {}", deviceCount, eventsPerSecond, eventTypes);
    }

    @Scheduled(fixedRateString = "${generator.interval-ms:100}")
    public void generate() {
        if (!enabled) return;

        for (int i = 0; i < eventsPerSecond / 10; i++) {
            TelemetryEvent event = createEvent();
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(targetUrl, event, String.class);
                long count = sentCount.incrementAndGet();
                if (count % 100 == 0) {
                    log.info("Отправлено {} событий, последнее: deviceId={}, eventType={}",
                            count, event.getDeviceId(), event.getEventType());
                }
            } catch (Exception e) {
                log.error("Ошибка отправки для {}: {}", event.getDeviceId(), e.getMessage());
            }
        }
    }

    private TelemetryEvent createEvent() {
        String deviceId = String.format("device-%03d", random.nextInt(deviceCount) + 1);
        String eventType = eventTypes.get(random.nextInt(eventTypes.size()));
        String payload = generatePayload(eventType);
        long timestamp = System.currentTimeMillis();
        return new TelemetryEvent(deviceId, eventType, payload, timestamp);
    }

    private String generatePayload(String eventType) {
        return switch (eventType) {
            case "HIGH_DEMAND" -> "demand_score=" + (random.nextInt(50) + 50);
            case "LOW_STOCK" -> "items_remaining=" + random.nextInt(10);
            case "TEMPERATURE_ALERT" -> "temperature=" + (random.nextInt(15) + 25) + "C";
            case "SALES_REPORT" -> "sales_count=" + random.nextInt(20) + ",revenue=" + (random.nextInt(5000) + 500);
            case "DOOR_OPENED" -> "duration_sec=" + (random.nextInt(30) + 5);
            default -> "data";
        };
    }
}

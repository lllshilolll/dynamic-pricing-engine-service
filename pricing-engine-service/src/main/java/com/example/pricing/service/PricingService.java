package com.example.pricing.service;

import com.example.dto.TelemetryEvent;
import com.example.pricing.dto.PriceUpdate;
import com.example.pricing.service.repository.PricingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
public class PricingService {
    private static final Logger log = LoggerFactory.getLogger(PricingService.class);
    private final PricingRepository pricingRepository;
    @Autowired
    private ObjectMapper objectMapper;

    public PricingService(PricingRepository pricingRepository) {
        this.pricingRepository = pricingRepository;
    }

    public Mono<PriceUpdate> calculateNewPrice(TelemetryEvent event) {
        var eventType = event.getEventType();
        var deviceId = event.getDeviceId();
        double coefficient = "HIGH_DEMAND".equals(eventType) ? 1.5 : 1.0;
        PriceUpdate update = new PriceUpdate(null, deviceId, coefficient, LocalDateTime.now());

        return pricingRepository.save(update)
                .doOnSuccess(res -> log.info("Price saved for {}: {}", deviceId, res.priceCoefficient()));
    }
}
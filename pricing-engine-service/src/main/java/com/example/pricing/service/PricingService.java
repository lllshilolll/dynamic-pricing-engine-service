package com.example.pricing.service;

import com.example.dto.TelemetryEvent;
import com.example.pricing.dto.PriceUpdate;
import com.example.pricing.service.redis.RedisService;
import com.example.pricing.service.repository.PricingRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@AllArgsConstructor
public class PricingService {
    private static final Logger log = LoggerFactory.getLogger(PricingService.class);
    private final PricingRepository pricingRepository;
    private final RedisService redisService;


    public void calculateNewPrice(String messageId, TelemetryEvent event) {
        var eventType = event.getEventType();
        var deviceId = event.getDeviceId();

        double coefficient = "HIGH_DEMAND".equals(eventType) ? 1.5 : 1.0;
        PriceUpdate update = new PriceUpdate(null, deviceId, coefficient, LocalDateTime.now());

        pricingRepository.save(update)
                .doOnSuccess(res -> {
                    log.info("Price saved for {}: {}", deviceId, res.getPriceCoefficient());
                    redisService.acknowledge(messageId);
                })
                .doOnError(err -> log.error("Price saving error for {}", deviceId, err))
                .subscribe();
    }
}
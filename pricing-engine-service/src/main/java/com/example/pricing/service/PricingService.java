package com.example.pricing.service;

import com.example.dto.TelemetryEvent;
import com.example.pricing.dto.PriceUpdate;
import com.example.pricing.service.exception.PersistenceException;
import com.example.pricing.service.redis.RedisService;
import com.example.pricing.service.repository.PricingRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Component
@AllArgsConstructor
public class PricingService {
    private final PricingRepository pricingRepository;
    private final RedisService redisService;

    public Mono<Void> calculateNewPrice(String messageId, TelemetryEvent event) {
        var eventType = event.getEventType();
        var deviceId = event.getDeviceId();

        double coefficient = "HIGH_DEMAND".equals(eventType) ? 1.5 : 1.0;
        PriceUpdate update = new PriceUpdate(null, deviceId, coefficient, LocalDateTime.now());

        return pricingRepository.save(update)
                .flatMap(saved -> {
                    log.info("Цена сохранена для {}: коэффициент={}", deviceId, saved.getPriceCoefficient());
                    return redisService.acknowledge(messageId);
                })
                .doOnError(err -> log.error("Ошибка обработки для устройства {}: {}", deviceId, err.getMessage()))
                .onErrorMap(e -> isMongoException(e)
                        ? new PersistenceException(e)
                        : e);
    }

    private boolean isMongoException(Throwable e) {
        return e.getClass().getName().toLowerCase().contains("mongo")
                || e.getClass().getName().toLowerCase().contains("dataaccess");
    }
}

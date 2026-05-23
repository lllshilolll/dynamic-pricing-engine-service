package com.example.pricing.service;

import com.example.dto.TelemetryEvent;
import com.example.pricing.dto.DeviceState;
import com.example.pricing.service.clickhouse.ClickHouseWriter;
import com.example.pricing.service.redis.RedisService;
import com.example.pricing.service.repository.DeviceStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PricingService {

    private final DeviceStateRepository deviceStateRepository;
    private final RedisService redisService;
    private final ClickHouseWriter clickHouseWriter;

    public Mono<Void> calculateNewPrice(TelemetryEvent event) {
        var eventType = event.getEventType();
        var deviceId = event.getDeviceId();
        double coefficient = "HIGH_DEMAND".equals(eventType) ? 1.5 : 1.0;

        return deviceStateRepository.findByDeviceId(deviceId)
                .map(existing -> {
                    double priceBefore = existing.getCurrentPrice();
                    existing.setCurrentPrice(coefficient);
                    existing.setLastEvent(eventType);
                    existing.setUpdatedAt(LocalDateTime.now());
                    return new DeviceStateDelta(existing, priceBefore, coefficient);
                })
                .defaultIfEmpty(new DeviceStateDelta(
                        new DeviceState(null, deviceId, coefficient, eventType, LocalDateTime.now()),
                        1.0,
                        coefficient
                ))
                .flatMap(delta -> deviceStateRepository.save(delta.state())
                        .flatMap(saved -> {
                            log.info("Цена обновлена для {}: {} → {}", deviceId, delta.priceBefore(), delta.priceAfter());
                            return Mono.when(
                                    redisService.cachePrice(deviceId, coefficient),
                                    clickHouseWriter.recordEvent(event, delta.priceBefore(), delta.priceAfter())
                            );
                        }))
                .doOnError(err -> log.error("Ошибка обработки для устройства {}: {}", deviceId, err.getMessage()));
    }

    private record DeviceStateDelta(DeviceState state, double priceBefore, double priceAfter) {}
}

package com.example.pricing.service.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
public class RedisService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public RedisService(ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Void> cachePrice(String deviceId, double price) {
        return redisTemplate.opsForValue()
                .set("price:" + deviceId, String.valueOf(price), Duration.ofMinutes(5))
                .doOnSuccess(v -> log.info("Цена закэширована для {}: {}", deviceId, price))
                .onErrorResume(err -> {
                    log.error("Ошибка кэширования цены для {}: {}", deviceId, err.getMessage());
                    return Mono.empty();
                })
                .then();
    }

    public Mono<String> getCachedPrice(String deviceId) {
        return redisTemplate.opsForValue().get("price:" + deviceId)
                .map(Object::toString);
    }
}

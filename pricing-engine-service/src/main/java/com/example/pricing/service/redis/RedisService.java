package com.example.pricing.service.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class RedisService {

    @Value("${redis.consumer.topic}")
    private String topic;
    @Value("${redis.consumer.group}")
    private String group;

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public RedisService(ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Void> acknowledge(String messageId) {
        return redisTemplate.opsForStream().acknowledge(topic, group, messageId)
                .doOnSuccess(ack -> log.info("Сообщение {} подтверждено (ACK) в Redis", messageId))
                .onErrorResume(err -> {
                    log.error("Ошибка подтверждения сообщения {} в Redis: {}", messageId, err.getMessage());
                    return Mono.empty();
                })
                .then();
    }
}
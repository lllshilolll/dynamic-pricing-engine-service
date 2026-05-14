package com.example.pricing.service.redis;

import com.example.dto.TelemetryEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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

    public Mono<String> saveData(ObjectRecord<String, TelemetryEvent> record) {
        return redisTemplate.opsForStream()
                .add(record)
                .map(RecordId::getValue);
    }

    public void acknowledge(String messageId) {
        redisTemplate.opsForStream().acknowledge(topic, group, messageId);
        System.out.println("messageId " + messageId + " удален");
    }
}
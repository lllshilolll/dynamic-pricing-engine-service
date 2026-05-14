package com.example.telemetry.service.redis;

import com.example.dto.TelemetryEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class RedisService {
    @Value("${redis.producer.topic}")
    private String topic;

    @Autowired
    private ObjectMapper objectMapper;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public RedisService(ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<String> saveData(TelemetryEvent telemetryEvent) {
        Map map = objectMapper.convertValue(telemetryEvent, Map.class);

        MapRecord<String, String, String> record = MapRecord.create(topic, map);

        return redisTemplate.opsForStream()
                .add(record)
                .map(RecordId::getValue);
    }
}
package com.example.telemetry.config;

import com.example.dto.TelemetryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Bean
    public ReactiveRedisTemplate<String, Object> ReactiveRedisConnectionFactory(ReactiveRedisConnectionFactory factory) {
        RedisSerializationContext<String, Object> serializationContext =
                RedisSerializationContext.<String, Object>newSerializationContext(new StringRedisSerializer())
                        .key(new StringRedisSerializer())
                        .value(new Jackson2JsonRedisSerializer<>(Object.class))
                        .hashKey(new StringRedisSerializer())
                        .hashValue(new Jackson2JsonRedisSerializer<>(Object.class))
                        .build();

        return new ReactiveRedisTemplate<>(factory, serializationContext);
    }
}
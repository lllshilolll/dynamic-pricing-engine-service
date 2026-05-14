package com.example.pricing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import java.time.Duration;

@Configuration
public class RedisStreamConfig {

    @Bean
    public Subscription subscription(RedisConnectionFactory redisConnectionFactory,
                                     StreamListener<String, MapRecord<String, String, String>> streamListener,
                                     @Value("${redis.consumer.topic}") String topic,
                                     @Value("${redis.consumer.group}") String consumerGroup,
                                     @Value("${redis.consumer.name}") String consumerName) {

        // Настройка контейнера
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .pollTimeout(Duration.ofSeconds(1))//Раз в 1 секунду контейнер «опрашивает» Redis: "Есть что новенькое?"
                        .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(redisConnectionFactory, options);

        // Подписка на поток (автоматическое чтение новых сообщений - $> )
        Subscription subscription = container.receive(
                Consumer.from(consumerGroup, consumerName),
                StreamOffset.create(topic, ReadOffset.lastConsumed()),
                streamListener
        );

        //запускает фоновый процесс опроса Redis.
        container.start();
        return subscription;
    }
}
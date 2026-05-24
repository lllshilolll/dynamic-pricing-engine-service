package com.example.pricing.config;

import com.example.dto.PriceChangeEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.sender.SenderOptions;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ReactiveKafkaProducerTemplate<String, PriceChangeEvent> priceChangeProducerTemplate(
            KafkaProperties kafkaProperties) {
        SenderOptions<String, PriceChangeEvent> options = SenderOptions.create(
                kafkaProperties.buildProducerProperties()
        );
        return new ReactiveKafkaProducerTemplate<>(options);
    }
}

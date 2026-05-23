package com.example.telemetry.config;

import com.example.dto.TelemetryEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.sender.SenderOptions;

@Configuration
public class KafkaConfig {

    @Bean
    public ReactiveKafkaProducerTemplate<String, TelemetryEvent> reactiveKafkaProducerTemplate(
            KafkaProperties kafkaProperties) {
        SenderOptions<String, TelemetryEvent> options = SenderOptions.create(
                kafkaProperties.buildProducerProperties()
        );
        return new ReactiveKafkaProducerTemplate<>(options);
    }
}

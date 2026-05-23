package com.example.telemetry.service.kafka;

import com.example.dto.TelemetryEvent;
import com.example.telemetry.service.exception.KafkaOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Service
public class KafkaProducerService {

    @Value("${kafka.topic.telemetry}")
    private String topic;

    private final ReactiveKafkaProducerTemplate<String, TelemetryEvent> producerTemplate;

    public KafkaProducerService(ReactiveKafkaProducerTemplate<String, TelemetryEvent> producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    public Mono<String> publish(TelemetryEvent event) {
        return producerTemplate.send(topic, event.getDeviceId(), event)
                .doOnNext(result -> log.info("Событие отправлено в Kafka: deviceId={}, offset={}",
                        event.getDeviceId(), result.recordMetadata().offset()))
                .map(result -> result.recordMetadata().offset() + "")
                .doOnError(error -> log.warn("Временная ошибка записи в Kafka, повторная попытка..."))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
                .onErrorMap(e -> {
                    log.error("Все попытки записи в Kafka исчерпаны: {}", e.getMessage());
                    return new KafkaOperationException(e);
                });
    }
}

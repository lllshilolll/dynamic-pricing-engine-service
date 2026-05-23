package com.example.telemetry.service.exception;

import org.springframework.http.HttpStatus;

public class KafkaOperationException extends BusinessException {

    public KafkaOperationException(Throwable cause) {
        super("Сервис временно недоступен. Ошибка записи в Kafka.", HttpStatus.SERVICE_UNAVAILABLE, cause);
    }
}

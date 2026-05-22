package com.example.telemetry.service.exception;

import org.springframework.http.HttpStatus;

public class RedisOperationException extends BusinessException {

    public RedisOperationException(Throwable cause) {
        super("Сервис временно недоступен. Проблемы со слоем кэширования/очередей.", HttpStatus.SERVICE_UNAVAILABLE, cause);
    }
}

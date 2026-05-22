package com.example.pricing.service.exception;

public class RedisOperationException extends BusinessException {

    public RedisOperationException(Throwable cause) {
        super("Ошибка взаимодействия с Redis.", cause);
    }
}

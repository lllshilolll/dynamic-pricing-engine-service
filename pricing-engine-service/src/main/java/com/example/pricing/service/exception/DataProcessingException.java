package com.example.pricing.service.exception;

public class DataProcessingException extends BusinessException {

    public DataProcessingException(Throwable cause) {
        super("Ошибка обработки данных.", cause);
    }
}

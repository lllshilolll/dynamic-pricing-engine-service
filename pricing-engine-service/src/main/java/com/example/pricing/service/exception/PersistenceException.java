package com.example.pricing.service.exception;

public class PersistenceException extends BusinessException {

    public PersistenceException(Throwable cause) {
        super("Ошибка сохранения данных.", cause);
    }
}

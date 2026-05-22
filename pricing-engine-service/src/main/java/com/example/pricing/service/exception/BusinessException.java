package com.example.pricing.service.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String userMessage;

    public BusinessException(String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.userMessage = userMessage;
    }
}

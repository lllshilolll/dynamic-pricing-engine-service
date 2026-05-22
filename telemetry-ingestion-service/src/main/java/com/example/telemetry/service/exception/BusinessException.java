package com.example.telemetry.service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final String userMessage;
    private final HttpStatus httpStatus;

    public BusinessException(String userMessage, HttpStatus httpStatus, Throwable cause) {
        super(userMessage, cause);
        this.userMessage = userMessage;
        this.httpStatus = httpStatus;
    }
}

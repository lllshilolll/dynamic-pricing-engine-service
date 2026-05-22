package com.example.telemetry.service.exception.handler;

import com.example.telemetry.service.exception.BusinessException;
import com.example.telemetry.service.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex,
                                                                  ServerWebExchange exchange) {
        log.error("[BUSINESS ERROR] {} {}: {}", exchange.getRequest().getPath(),
                ex.getHttpStatus(), ex.getUserMessage(), ex.getCause());

        return ResponseEntity.status(ex.getHttpStatus())
                .body(new ErrorResponse(
                        exchange.getRequest().getPath().value(),
                        ex.getHttpStatus().value(),
                        ex.getHttpStatus().getReasonPhrase(),
                        ex.getUserMessage(),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex,
                                                                 ServerWebExchange exchange) {
        log.error("[UNKNOWN ERROR] Непредвиденная ошибка: ", ex);

        return ResponseEntity.internalServerError()
                .body(new ErrorResponse(
                        exchange.getRequest().getPath().value(),
                        500,
                        "Internal Server Error",
                        "Внутренняя ошибка сервера",
                        LocalDateTime.now()
                ));
    }
}

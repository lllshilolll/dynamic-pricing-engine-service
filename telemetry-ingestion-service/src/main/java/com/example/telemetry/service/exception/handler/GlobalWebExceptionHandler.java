package com.example.telemetry.service.exception.handler;

import com.example.telemetry.service.exception.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Component
@Order(-2) // Высокий приоритет: наш обработчик должен сработать раньше стандартного спрингового
public class GlobalWebExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalWebExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        
        // По умолчанию отдаем 500 Internal Server Error
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String clientMessage = "Внутренняя ошибка сервера";

        // Определяем тип ошибки. Если это проблемы с Redis (сетью/драйвером)
        if (ex.getMessage() != null && (ex.getMessage().contains("Redis") || ex.getClass().getName().contains("redis"))) {
            status = HttpStatus.SERVICE_UNAVAILABLE; // 503
            clientMessage = "Сервис временно недоступен. Проблемы со слоем кэширования/очередей.";
            log.error("[REDIS ERROR] Ошибка записи в Redis Streams на путях {}: {}", exchange.getRequest().getPath(), ex.getMessage());
        } else {
            log.error("[UNKNOWN ERROR] Непредвиденная ошибка при обработке запроса: ", ex);
        }

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Строим красивое DTO
        ErrorResponse errorBody = new ErrorResponse(
                exchange.getRequest().getPath().value(),
                status.value(),
                status.getReasonPhrase(),
                clientMessage,
                LocalDateTime.now()
        );

        // Сериализуем в байты и пишем в ответ
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorBody);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации ответа об ошибке", e);
            return Mono.error(e);
        }
    }
}
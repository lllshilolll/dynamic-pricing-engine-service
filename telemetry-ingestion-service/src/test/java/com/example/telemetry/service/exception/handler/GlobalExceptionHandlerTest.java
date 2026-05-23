package com.example.telemetry.service.exception.handler;

import com.example.telemetry.service.TelemetryService;
import com.example.telemetry.service.exception.BusinessException;
import com.example.telemetry.service.exception.KafkaOperationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
class GlobalExceptionHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private TelemetryService telemetryService;

    @Test
    void handle_BusinessException_ShouldReturnCorrespondingStatus() {
        when(telemetryService.saveTelemetry(any())).thenReturn(
                Mono.error(new KafkaOperationException(new RuntimeException("Kafka down"))));

        webTestClient.post()
                .uri("/api/telemetry")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"deviceId\":\"d1\",\"eventType\":\"HIGH_DEMAND\",\"payload\":\"t\",\"timestamp\":1}")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Сервис временно недоступен. Ошибка записи в Kafka.")
                .jsonPath("$.status").isEqualTo(503);
    }

    @Test
    void handle_GenericException_ShouldReturn500() {
        when(telemetryService.saveTelemetry(any())).thenReturn(
                Mono.error(new RuntimeException("unexpected")));

        webTestClient.post()
                .uri("/api/telemetry")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"deviceId\":\"d1\",\"eventType\":\"HIGH_DEMAND\",\"payload\":\"t\",\"timestamp\":1}")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Внутренняя ошибка сервера")
                .jsonPath("$.status").isEqualTo(500);
    }

    @Test
    void handle_CustomBusinessException_ShouldReturnItsStatus() {
        when(telemetryService.saveTelemetry(any())).thenReturn(
                Mono.error(new BusinessException("Ошибка данных",
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        new IllegalArgumentException("bad"))));

        webTestClient.post()
                .uri("/api/telemetry")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"deviceId\":\"d1\",\"eventType\":\"HIGH_DEMAND\",\"payload\":\"t\",\"timestamp\":1}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Ошибка данных")
                .jsonPath("$.status").isEqualTo(400);
    }
}

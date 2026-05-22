package com.example.telemetry.service.controller;

import com.example.telemetry.service.TelemetryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(TelemetryController.class)
class TelemetryControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private TelemetryService telemetryService;

    @Test
    void process_ShouldReturnAcceptedWithRecordId() {
        when(telemetryService.saveTelemetry(any())).thenReturn(Mono.just("1715712000000-0"));

        webTestClient.post()
                .uri("/api/telemetry")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"deviceId\":\"device-1\",\"eventType\":\"HIGH_DEMAND\",\"payload\":\"test\",\"timestamp\":1000}")
                .exchange()
                .expectStatus().isAccepted()
                .expectBody(String.class)
                .isEqualTo("1715712000000-0");
    }

    @Test
    void process_WhenServiceFails_ShouldReturnServerError() {
        when(telemetryService.saveTelemetry(any())).thenReturn(Mono.error(new RuntimeException("Redis down")));

        webTestClient.post()
                .uri("/api/telemetry")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"deviceId\":\"device-1\",\"eventType\":\"HIGH_DEMAND\",\"payload\":\"test\",\"timestamp\":1000}")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void process_WhenEmptyBody_ShouldReturnServerError() {
        webTestClient.post()
                .uri("/api/telemetry")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}

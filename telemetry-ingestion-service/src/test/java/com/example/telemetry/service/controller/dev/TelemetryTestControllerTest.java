package com.example.telemetry.service.controller.dev;

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

@WebFluxTest(TelemetryTestController.class)
public class TelemetryTestControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private TelemetryService telemetryService;

    @Test
    void sendTestEvent() {
        var telemetryId = "1778765056349-0";
        when(telemetryService.saveTelemetry(any())).thenReturn(Mono.just(telemetryId));

        webTestClient.get()
                .uri("/api/test/send")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(telemetryId);
    }

    @Test
    void sendTestEventError() {
        when(telemetryService.saveTelemetry(any())).thenThrow(new RuntimeException());

        webTestClient.get()
                .uri("/api/test/send")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Внутренняя ошибка сервера");

    }
}

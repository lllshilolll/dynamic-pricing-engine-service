package com.example.telemetry.service.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        String path,
        int status,
        String error,
        String message,
        LocalDateTime timestamp
) {}
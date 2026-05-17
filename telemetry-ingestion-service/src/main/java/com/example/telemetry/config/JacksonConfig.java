package com.example.telemetry.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary // Этот бин будет главным в контексте приложения
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Поддержка Java 8 Date/Time API (LocalDateTime, Instant и т.д.)
        mapper.registerModule(new JavaTimeModule());

        // Отключаем запись дат в виде таймстампов (чтобы в JSON была красивая строка)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Если клиент пришлет лишние поля в JSON, приложение не упадет
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }
}
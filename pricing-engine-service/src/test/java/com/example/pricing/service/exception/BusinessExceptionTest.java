package com.example.pricing.service.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void redisOperationException_ShouldHaveCorrectMessage() {
        RedisOperationException ex = new RedisOperationException(new RuntimeException("timeout"));
        assertEquals("Ошибка взаимодействия с Redis.", ex.getUserMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    void persistenceException_ShouldHaveCorrectMessage() {
        PersistenceException ex = new PersistenceException(new RuntimeException("mongo fail"));
        assertEquals("Ошибка сохранения данных.", ex.getUserMessage());
    }

    @Test
    void dataProcessingException_ShouldHaveCorrectMessage() {
        DataProcessingException ex = new DataProcessingException(new IllegalArgumentException("bad json"));
        assertEquals("Ошибка обработки данных.", ex.getUserMessage());
    }

    @Test
    void businessException_ShouldPreserveCause() {
        RuntimeException cause = new RuntimeException("root cause");
        BusinessException ex = new BusinessException("user msg", cause);
        assertEquals("user msg", ex.getUserMessage());
        assertSame(cause, ex.getCause());
    }
}

package com.example.pricing.service.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class BusinessExceptionTest {

    @Test
    void businessException_ShouldPreserveCause() {
        RuntimeException cause = new RuntimeException("root cause");
        BusinessException ex = new BusinessException("user msg", cause);
        assertEquals("user msg", ex.getUserMessage());
        assertSame(cause, ex.getCause());
    }
}

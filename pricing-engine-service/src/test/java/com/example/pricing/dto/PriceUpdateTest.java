package com.example.pricing.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PriceUpdateTest {

    @Test
    void constructor_ShouldSetAllFields() {
        LocalDateTime now = LocalDateTime.now();
        PriceUpdate update = new PriceUpdate("id1", "device-1", 1.5, now);

        assertEquals("id1", update.getId());
        assertEquals("device-1", update.getDeviceId());
        assertEquals(1.5, update.getPriceCoefficient());
        assertEquals(now, update.getCreatedAt());
    }

    @Test
    void constructor_WithNullId_ShouldAllowNull() {
        PriceUpdate update = new PriceUpdate(null, "device-2", 1.0, null);

        assertNull(update.getId());
        assertEquals("device-2", update.getDeviceId());
        assertEquals(1.0, update.getPriceCoefficient());
        assertNull(update.getCreatedAt());
    }
}

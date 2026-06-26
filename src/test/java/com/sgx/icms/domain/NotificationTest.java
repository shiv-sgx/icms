package com.sgx.icms.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class NotificationTest {

    @Test
    void gettersAndSettersRoundTrip() {
        Notification n = new Notification();
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 8, 0);

        n.setId(1L);
        n.setUserId(2L);
        n.setTargetRole("AGENT");
        n.setType("URGENT");
        n.setMessage("New claim assigned");
        n.setRead(true);
        n.setCreatedAt(createdAt);

        assertEquals(1L, n.getId());
        assertEquals(2L, n.getUserId());
        assertEquals("AGENT", n.getTargetRole());
        assertEquals("URGENT", n.getType());
        assertEquals("New claim assigned", n.getMessage());
        assertTrue(n.isRead());
        assertEquals(createdAt, n.getCreatedAt());
    }

    @Test
    void readDefaultsFalse() {
        Notification n = new Notification();
        assertFalse(n.isRead());
    }
}

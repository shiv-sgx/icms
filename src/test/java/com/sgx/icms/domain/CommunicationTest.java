package com.sgx.icms.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class CommunicationTest {

    @Test
    void gettersAndSettersRoundTrip() {
        Communication c = new Communication();
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);

        c.setId(1L);
        c.setClaimId(2L);
        c.setSenderId(3L);
        c.setSenderName("Agent A");
        c.setChannel("EMAIL");
        c.setContent("Please upload your documents");
        c.setCreatedAt(createdAt);
        c.setClaimNo("CLM-2026-0001");

        assertEquals(1L, c.getId());
        assertEquals(2L, c.getClaimId());
        assertEquals(3L, c.getSenderId());
        assertEquals("Agent A", c.getSenderName());
        assertEquals("EMAIL", c.getChannel());
        assertEquals("Please upload your documents", c.getContent());
        assertEquals(createdAt, c.getCreatedAt());
        assertEquals("CLM-2026-0001", c.getClaimNo());
    }
}

package com.sgx.icms.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class AuditLogTest {

    @Test
    void gettersAndSettersRoundTrip() {
        AuditLog log = new AuditLog();
        LocalDateTime ts = LocalDateTime.of(2026, 1, 1, 9, 0);

        log.setId(1L);
        log.setTs(ts);
        log.setUserId(2L);
        log.setUsername("bob");
        log.setRole("AGENT");
        log.setAction("LOGIN");
        log.setEntity("user:bob");
        log.setIpAddress("127.0.0.1");
        log.setResult(AuditLog.RESULT_SUCCESS);

        assertEquals(1L, log.getId());
        assertEquals(ts, log.getTs());
        assertEquals(2L, log.getUserId());
        assertEquals("bob", log.getUsername());
        assertEquals("AGENT", log.getRole());
        assertEquals("LOGIN", log.getAction());
        assertEquals("user:bob", log.getEntity());
        assertEquals("127.0.0.1", log.getIpAddress());
        assertEquals(AuditLog.RESULT_SUCCESS, log.getResult());
    }

    @Test
    void resultConstantsHaveExpectedValues() {
        assertEquals("SUCCESS", AuditLog.RESULT_SUCCESS);
        assertEquals("FAILED", AuditLog.RESULT_FAILED);
    }
}

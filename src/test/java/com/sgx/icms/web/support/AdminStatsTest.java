package com.sgx.icms.web.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AdminStatsTest {

    @Test
    void gettersAndSettersRoundTrip() {
        AdminStats s = new AdminStats();
        s.setUsers(10L);
        s.setClaims(20L);
        s.setRoles(5L);
        s.setAuditEvents(100L);
        s.setPoolActive(2);
        s.setPoolIdle(8);
        s.setPoolTotal(10);

        assertEquals(10L, s.getUsers());
        assertEquals(20L, s.getClaims());
        assertEquals(5L, s.getRoles());
        assertEquals(100L, s.getAuditEvents());
        assertEquals(2, s.getPoolActive());
        assertEquals(8, s.getPoolIdle());
        assertEquals(10, s.getPoolTotal());
    }
}

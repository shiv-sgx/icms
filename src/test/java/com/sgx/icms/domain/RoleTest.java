package com.sgx.icms.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    void gettersAndSettersRoundTrip() {
        Role r = new Role();
        r.setId(1);
        r.setName("AGENT");
        r.setDescription("Field agent");
        r.setUserCount(5L);

        assertEquals(1, r.getId());
        assertEquals("AGENT", r.getName());
        assertEquals("Field agent", r.getDescription());
        assertEquals(5L, r.getUserCount());
    }
}

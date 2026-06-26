package com.sgx.icms.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SlaConfigTest {

    @Test
    void gettersAndSettersRoundTrip() {
        SlaConfig c = new SlaConfig();
        c.setId(1);
        c.setStage("UNDER_REVIEW");
        c.setHours(24);

        assertEquals(1, c.getId());
        assertEquals("UNDER_REVIEW", c.getStage());
        assertEquals(24, c.getHours());
    }
}

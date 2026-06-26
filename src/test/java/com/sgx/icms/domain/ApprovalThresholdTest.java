package com.sgx.icms.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class ApprovalThresholdTest {

    @Test
    void gettersAndSettersRoundTrip() {
        ApprovalThreshold t = new ApprovalThreshold();
        t.setId(1);
        t.setLevel("L2");
        t.setLabel("Mid tier");
        t.setMinAmount(new BigDecimal("10000"));
        t.setMaxAmount(new BigDecimal("50000"));

        assertEquals(1, t.getId());
        assertEquals("L2", t.getLevel());
        assertEquals("Mid tier", t.getLabel());
        assertEquals(new BigDecimal("10000"), t.getMinAmount());
        assertEquals(new BigDecimal("50000"), t.getMaxAmount());
    }

    @Test
    void maxAmountMayBeNullForNoUpperBound() {
        ApprovalThreshold t = new ApprovalThreshold();
        t.setMaxAmount(null);
        assertNull(t.getMaxAmount());
    }
}

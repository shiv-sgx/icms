package com.sgx.icms.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class PolicyTest {

    @Test
    void gettersAndSettersRoundTrip() {
        Policy p = new Policy();
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate expiryDate = LocalDate.of(2026, 1, 1);

        p.setId(1L);
        p.setPolicyNo("POL-84521");
        p.setPolicyholderId(2L);
        p.setType("MOTOR");
        p.setSumInsured(new BigDecimal("500000"));
        p.setPremium(new BigDecimal("12000"));
        p.setStartDate(startDate);
        p.setExpiryDate(expiryDate);
        p.setNcbDiscount(new BigDecimal("20"));
        p.setStatus("ACTIVE");

        assertEquals(1L, p.getId());
        assertEquals("POL-84521", p.getPolicyNo());
        assertEquals(2L, p.getPolicyholderId());
        assertEquals("MOTOR", p.getType());
        assertEquals(new BigDecimal("500000"), p.getSumInsured());
        assertEquals(new BigDecimal("12000"), p.getPremium());
        assertEquals(startDate, p.getStartDate());
        assertEquals(expiryDate, p.getExpiryDate());
        assertEquals(new BigDecimal("20"), p.getNcbDiscount());
        assertEquals("ACTIVE", p.getStatus());
    }

    @Test
    void displayLabelCombinesPolicyNoAndType() {
        Policy p = new Policy();
        p.setPolicyNo("POL-84521");
        p.setType("MOTOR");
        assertEquals("POL-84521 — MOTOR", p.getDisplayLabel());
    }
}

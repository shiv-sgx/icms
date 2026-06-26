package com.sgx.icms.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class SettlementTest {

    @Test
    void gettersAndSettersRoundTrip() {
        Settlement s = new Settlement();
        LocalDateTime approvalDate = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime paymentInitiatedAt = LocalDateTime.of(2026, 1, 2, 10, 0);
        LocalDateTime paymentConfirmedAt = LocalDateTime.of(2026, 1, 3, 10, 0);
        LocalDateTime closedAt = LocalDateTime.of(2026, 1, 4, 10, 0);
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 9, 0);

        s.setId(1L);
        s.setClaimId(2L);
        s.setFinalAmount(new BigDecimal("75000"));
        s.setJustification("Approved after assessment");
        s.setPaymentMethod("NEFT");
        s.setAccountHolder("John Doe");
        s.setBankName("Bank of Test");
        s.setAccountNumber("1234567890");
        s.setIfscCode("TEST0001234");
        s.setStatus(Settlement.AUTHORIZED);
        s.setApprovedBy(3L);
        s.setApprovalDate(approvalDate);
        s.setPaymentInitiatedAt(paymentInitiatedAt);
        s.setPaymentConfirmedAt(paymentConfirmedAt);
        s.setClosedAt(closedAt);
        s.setCreatedAt(createdAt);

        assertEquals(1L, s.getId());
        assertEquals(2L, s.getClaimId());
        assertEquals(new BigDecimal("75000"), s.getFinalAmount());
        assertEquals("Approved after assessment", s.getJustification());
        assertEquals("NEFT", s.getPaymentMethod());
        assertEquals("John Doe", s.getAccountHolder());
        assertEquals("Bank of Test", s.getBankName());
        assertEquals("1234567890", s.getAccountNumber());
        assertEquals("TEST0001234", s.getIfscCode());
        assertEquals(Settlement.AUTHORIZED, s.getStatus());
        assertEquals(3L, s.getApprovedBy());
        assertEquals(approvalDate, s.getApprovalDate());
        assertEquals(paymentInitiatedAt, s.getPaymentInitiatedAt());
        assertEquals(paymentConfirmedAt, s.getPaymentConfirmedAt());
        assertEquals(closedAt, s.getClosedAt());
        assertEquals(createdAt, s.getCreatedAt());
    }

    @Test
    void trackerListIsInExpectedOrder() {
        assertEquals(6, Settlement.TRACKER.size());
        assertEquals(Settlement.AUTHORIZED, Settlement.TRACKER.get(0));
        assertEquals(Settlement.CLOSED, Settlement.TRACKER.get(Settlement.TRACKER.size() - 1));
    }
}

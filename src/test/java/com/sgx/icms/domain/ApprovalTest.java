package com.sgx.icms.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class ApprovalTest {

    @Test
    void gettersAndSettersRoundTrip() {
        Approval a = new Approval();
        LocalDateTime decidedAt = LocalDateTime.of(2026, 1, 2, 3, 4);
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);

        a.setId(1L);
        a.setClaimId(2L);
        a.setLevel("L1");
        a.setApproverId(3L);
        a.setApproverRole("MANAGER");
        a.setDecision(Approval.APPROVED);
        a.setRemarks("looks fine");
        a.setDecidedAt(decidedAt);
        a.setCreatedAt(createdAt);
        a.setApproverName("Jane Doe");

        assertEquals(1L, a.getId());
        assertEquals(2L, a.getClaimId());
        assertEquals("L1", a.getLevel());
        assertEquals(3L, a.getApproverId());
        assertEquals("MANAGER", a.getApproverRole());
        assertEquals(Approval.APPROVED, a.getDecision());
        assertEquals("looks fine", a.getRemarks());
        assertEquals(decidedAt, a.getDecidedAt());
        assertEquals(createdAt, a.getCreatedAt());
        assertEquals("Jane Doe", a.getApproverName());
    }

    @Test
    void isPendingTrueWhenDecisionIsPending() {
        Approval a = new Approval();
        a.setDecision(Approval.PENDING);
        assertTrue(a.isPending());
    }

    @Test
    void isPendingFalseWhenDecisionIsSomethingElse() {
        Approval a = new Approval();
        a.setDecision(Approval.APPROVED);
        assertFalse(a.isPending());
    }

    @Test
    void isPendingFalseWhenDecisionIsNull() {
        Approval a = new Approval();
        assertFalse(a.isPending());
    }

    @Test
    void statusConstantsHaveExpectedValues() {
        assertEquals("PENDING", Approval.PENDING);
        assertEquals("APPROVED", Approval.APPROVED);
        assertEquals("CONDITIONAL", Approval.CONDITIONAL);
        assertEquals("REJECTED", Approval.REJECTED);
        assertEquals("RETURNED", Approval.RETURNED);
        assertEquals("ON_HOLD", Approval.ON_HOLD);
    }
}

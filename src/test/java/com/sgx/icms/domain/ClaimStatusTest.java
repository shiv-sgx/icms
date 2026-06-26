package com.sgx.icms.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ClaimStatusTest {

    @Test
    void lifecycleIndexReturnsPositionForHappyPathStatuses() {
        assertEquals(0, ClaimStatus.lifecycleIndex(ClaimStatus.SUBMITTED));
        assertEquals(8, ClaimStatus.lifecycleIndex(ClaimStatus.CLOSED));
    }

    @Test
    void lifecycleIndexReturnsMinusOneForOffPathOrUnknownStatus() {
        assertEquals(-1, ClaimStatus.lifecycleIndex(ClaimStatus.REJECTED));
        assertEquals(-1, ClaimStatus.lifecycleIndex("NOT_A_STATUS"));
        assertEquals(-1, ClaimStatus.lifecycleIndex(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {ClaimStatus.CLOSED, ClaimStatus.REJECTED, ClaimStatus.WITHDRAWN})
    void isTerminalTrueForTerminalStatuses(String status) {
        assertTrue(ClaimStatus.isTerminal(status));
    }

    @ParameterizedTest
    @ValueSource(strings = {ClaimStatus.DRAFT, ClaimStatus.SUBMITTED, ClaimStatus.APPROVED, ClaimStatus.ON_HOLD})
    void isTerminalFalseForNonTerminalStatuses(String status) {
        assertFalse(ClaimStatus.isTerminal(status));
    }

    @Test
    void isTerminalFalseForNull() {
        assertFalse(ClaimStatus.isTerminal(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            ClaimStatus.DRAFT, ClaimStatus.SUBMITTED, ClaimStatus.UNDER_REVIEW,
            ClaimStatus.SURVEY_SCHEDULED, ClaimStatus.UNDER_ASSESSMENT,
            ClaimStatus.PENDING_APPROVAL, ClaimStatus.ON_HOLD
    })
    void isWithdrawableTrueForEarlyOrHoldStatuses(String status) {
        assertTrue(ClaimStatus.isWithdrawable(status));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            ClaimStatus.APPROVED, ClaimStatus.SETTLEMENT_PROCESSING, ClaimStatus.SETTLED,
            ClaimStatus.CLOSED, ClaimStatus.REJECTED, ClaimStatus.WITHDRAWN
    })
    void isWithdrawableFalseForLateOrTerminalStatuses(String status) {
        assertFalse(ClaimStatus.isWithdrawable(status));
    }

    @Test
    void isWithdrawableFalseForNull() {
        assertFalse(ClaimStatus.isWithdrawable(null));
    }

    @Test
    void labelTitleCasesEachUnderscoreSeparatedWord() {
        assertEquals("Pending Approval", ClaimStatus.label(ClaimStatus.PENDING_APPROVAL));
        assertEquals("Closed", ClaimStatus.label(ClaimStatus.CLOSED));
    }

    @Test
    void labelReturnsEmptyStringForNull() {
        assertEquals("", ClaimStatus.label(null));
    }

    @Test
    void labelSkipsEmptySegmentsFromLeadingOrDoubleUnderscore() {
        assertEquals("Foo Bar", ClaimStatus.label("_FOO__BAR"));
    }

    @Test
    void pillReturnsOkForPositiveTerminalStatuses() {
        assertEquals("pill-ok", ClaimStatus.pill(ClaimStatus.SETTLED));
        assertEquals("pill-ok", ClaimStatus.pill(ClaimStatus.APPROVED));
        assertEquals("pill-ok", ClaimStatus.pill(ClaimStatus.CLOSED));
    }

    @Test
    void pillReturnsDangerForNegativeStatuses() {
        assertEquals("pill-danger", ClaimStatus.pill(ClaimStatus.REJECTED));
        assertEquals("pill-danger", ClaimStatus.pill(ClaimStatus.WITHDRAWN));
    }

    @Test
    void pillReturnsWarnForOnHoldOrPendingApproval() {
        assertEquals("pill-warn", ClaimStatus.pill(ClaimStatus.ON_HOLD));
        assertEquals("pill-warn", ClaimStatus.pill(ClaimStatus.PENDING_APPROVAL));
    }

    @Test
    void pillReturnsMutedForDraftOrNull() {
        assertEquals("pill-muted", ClaimStatus.pill(ClaimStatus.DRAFT));
        assertEquals("pill-muted", ClaimStatus.pill(null));
    }

    @Test
    void pillReturnsInfoForUnmatchedStatus() {
        assertEquals("pill-info", ClaimStatus.pill(ClaimStatus.SUBMITTED));
    }

    @Test
    void lifecycleListIsInExpectedOrder() {
        assertEquals(9, ClaimStatus.LIFECYCLE.size());
        assertEquals(ClaimStatus.SUBMITTED, ClaimStatus.LIFECYCLE.get(0));
        assertEquals(ClaimStatus.CLOSED, ClaimStatus.LIFECYCLE.get(ClaimStatus.LIFECYCLE.size() - 1));
    }
}

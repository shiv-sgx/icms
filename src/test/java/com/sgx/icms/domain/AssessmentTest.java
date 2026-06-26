package com.sgx.icms.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class AssessmentTest {

    @Test
    void gettersAndSettersRoundTrip() {
        Assessment a = new Assessment();
        LocalDate visitDate = LocalDate.of(2026, 3, 1);
        LocalTime visitTime = LocalTime.of(10, 30);
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 1, 11, 0);

        a.setId(1L);
        a.setClaimId(2L);
        a.setSurveyorId(3L);
        a.setVisitDate(visitDate);
        a.setVisitTime(visitTime);
        a.setSiteObservations("All good");
        a.setReportRefNo("RPT-1");
        a.setGrossAssessed(new BigDecimal("1000"));
        a.setPolicyDeductible(new BigDecimal("100"));
        a.setDepreciationPct(new BigDecimal("10"));
        a.setDepreciationAmt(new BigDecimal("90"));
        a.setSalvageValue(new BigDecimal("50"));
        a.setNetPayable(new BigDecimal("760"));
        a.setRecommendation("APPROVE_FULL");
        a.setRemarks("Approved as-is");
        a.setStatus(Assessment.STATUS_SUBMITTED);
        a.setCreatedAt(createdAt);
        a.setSurveyorName("S. Surveyor");

        assertEquals(1L, a.getId());
        assertEquals(2L, a.getClaimId());
        assertEquals(3L, a.getSurveyorId());
        assertEquals(visitDate, a.getVisitDate());
        assertEquals(visitTime, a.getVisitTime());
        assertEquals("All good", a.getSiteObservations());
        assertEquals("RPT-1", a.getReportRefNo());
        assertEquals(new BigDecimal("1000"), a.getGrossAssessed());
        assertEquals(new BigDecimal("100"), a.getPolicyDeductible());
        assertEquals(new BigDecimal("10"), a.getDepreciationPct());
        assertEquals(new BigDecimal("90"), a.getDepreciationAmt());
        assertEquals(new BigDecimal("50"), a.getSalvageValue());
        assertEquals(new BigDecimal("760"), a.getNetPayable());
        assertEquals("APPROVE_FULL", a.getRecommendation());
        assertEquals("Approved as-is", a.getRemarks());
        assertEquals(Assessment.STATUS_SUBMITTED, a.getStatus());
        assertEquals(createdAt, a.getCreatedAt());
        assertEquals("S. Surveyor", a.getSurveyorName());
    }

    @Test
    void statusConstantsHaveExpectedValues() {
        assertEquals("ASSIGNED", Assessment.STATUS_ASSIGNED);
        assertEquals("IN_PROGRESS", Assessment.STATUS_IN_PROGRESS);
        assertEquals("SUBMITTED", Assessment.STATUS_SUBMITTED);
    }
}

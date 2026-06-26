package com.sgx.icms.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class ClaimTest {

    @Test
    void gettersAndSettersRoundTrip() {
        Claim c = new Claim();
        LocalDate incidentDate = LocalDate.of(2026, 1, 1);
        LocalTime incidentTime = LocalTime.of(8, 0);
        LocalDate slaDueDate = LocalDate.of(2026, 1, 10);
        LocalDateTime filedAt = LocalDateTime.of(2026, 1, 1, 9, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 9, 0);

        c.setId(1L);
        c.setClaimNo("CLM-2026-0001");
        c.setPolicyId(2L);
        c.setPolicyholderId(3L);
        c.setClaimantName("John Doe");
        c.setClaimType("MOTOR");
        c.setClaimSubtype("OWN_DAMAGE");
        c.setIncidentDate(incidentDate);
        c.setIncidentTime(incidentTime);
        c.setIncidentLocation("Highway 1");
        c.setCity("Pune");
        c.setState("MH");
        c.setPinCode("411001");
        c.setDescription("Fender bender");
        c.setEstimatedLoss(new BigDecimal("25000"));
        c.setVehicleRegNo("MH12AB1234");
        c.setFirNumber("FIR-1");
        c.setPoliceStation("Station 1");
        c.setHospitalName("City Hospital");
        c.setWorkshopName("Acme Garage");
        c.setThirdParty("None");
        c.setStatus(ClaimStatus.SUBMITTED);
        c.setAgentId(4L);
        c.setSurveyorId(5L);
        c.setRiskLevel("LOW");
        c.setFraudScore(10);
        c.setInternalNotes("Looks fine");
        c.setSlaDueDate(slaDueDate);
        c.setFiledAt(filedAt);
        c.setUpdatedAt(updatedAt);
        c.setPolicyNo("POL-1");
        c.setAgentName("Agent A");
        c.setSurveyorName("Surveyor S");

        assertEquals(1L, c.getId());
        assertEquals("CLM-2026-0001", c.getClaimNo());
        assertEquals(2L, c.getPolicyId());
        assertEquals(3L, c.getPolicyholderId());
        assertEquals("John Doe", c.getClaimantName());
        assertEquals("MOTOR", c.getClaimType());
        assertEquals("OWN_DAMAGE", c.getClaimSubtype());
        assertEquals(incidentDate, c.getIncidentDate());
        assertEquals(incidentTime, c.getIncidentTime());
        assertEquals("Highway 1", c.getIncidentLocation());
        assertEquals("Pune", c.getCity());
        assertEquals("MH", c.getState());
        assertEquals("411001", c.getPinCode());
        assertEquals("Fender bender", c.getDescription());
        assertEquals(new BigDecimal("25000"), c.getEstimatedLoss());
        assertEquals("MH12AB1234", c.getVehicleRegNo());
        assertEquals("FIR-1", c.getFirNumber());
        assertEquals("Station 1", c.getPoliceStation());
        assertEquals("City Hospital", c.getHospitalName());
        assertEquals("Acme Garage", c.getWorkshopName());
        assertEquals("None", c.getThirdParty());
        assertEquals(ClaimStatus.SUBMITTED, c.getStatus());
        assertEquals(4L, c.getAgentId());
        assertEquals(5L, c.getSurveyorId());
        assertEquals("LOW", c.getRiskLevel());
        assertEquals(10, c.getFraudScore());
        assertEquals("Looks fine", c.getInternalNotes());
        assertEquals(slaDueDate, c.getSlaDueDate());
        assertEquals(filedAt, c.getFiledAt());
        assertEquals(updatedAt, c.getUpdatedAt());
        assertEquals("POL-1", c.getPolicyNo());
        assertEquals("Agent A", c.getAgentName());
        assertEquals("Surveyor S", c.getSurveyorName());
    }

    @Test
    void derivedHelpersDelegateToClaimStatus() {
        Claim c = new Claim();
        c.setStatus(ClaimStatus.APPROVED);

        assertEquals(ClaimStatus.label(ClaimStatus.APPROVED), c.getStatusLabel());
        assertEquals(ClaimStatus.pill(ClaimStatus.APPROVED), c.getStatusPill());
        assertFalse(c.isWithdrawable());
    }

    @Test
    void isWithdrawableTrueForEarlyStatus() {
        Claim c = new Claim();
        c.setStatus(ClaimStatus.SUBMITTED);
        assertTrue(c.isWithdrawable());
    }
}

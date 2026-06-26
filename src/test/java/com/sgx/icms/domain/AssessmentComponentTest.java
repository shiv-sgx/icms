package com.sgx.icms.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class AssessmentComponentTest {

    @Test
    void gettersAndSettersRoundTrip() {
        AssessmentComponent c = new AssessmentComponent();
        c.setId(1L);
        c.setAssessmentId(2L);
        c.setComponent("Bumper");
        c.setSeverity("MODERATE");
        c.setRepairCost(new BigDecimal("500"));
        c.setReplaceFlag(true);

        assertEquals(1L, c.getId());
        assertEquals(2L, c.getAssessmentId());
        assertEquals("Bumper", c.getComponent());
        assertEquals("MODERATE", c.getSeverity());
        assertEquals(new BigDecimal("500"), c.getRepairCost());
        assertTrue(c.isReplaceFlag());
    }

    @Test
    void replaceFlagDefaultsFalse() {
        AssessmentComponent c = new AssessmentComponent();
        assertFalse(c.isReplaceFlag());
    }
}

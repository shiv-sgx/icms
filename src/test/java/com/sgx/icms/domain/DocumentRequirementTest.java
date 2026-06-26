package com.sgx.icms.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DocumentRequirementTest {

    @Test
    void gettersAndSettersRoundTrip() {
        DocumentRequirement r = new DocumentRequirement();
        r.setId(1);
        r.setClaimType("MOTOR");
        r.setClaimSubtype("OWN_DAMAGE");
        r.setDocType("RC_BOOK");
        r.setRequired(true);

        assertEquals(1, r.getId());
        assertEquals("MOTOR", r.getClaimType());
        assertEquals("OWN_DAMAGE", r.getClaimSubtype());
        assertEquals("RC_BOOK", r.getDocType());
        assertTrue(r.isRequired());
    }

    @Test
    void requiredDefaultsFalse() {
        DocumentRequirement r = new DocumentRequirement();
        assertFalse(r.isRequired());
    }
}

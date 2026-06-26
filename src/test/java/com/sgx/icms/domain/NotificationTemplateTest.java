package com.sgx.icms.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NotificationTemplateTest {

    @Test
    void gettersAndSettersRoundTrip() {
        NotificationTemplate t = new NotificationTemplate();
        t.setId(1);
        t.setName("CLAIM_SUBMITTED");
        t.setChannel("SMS");
        t.setActive(true);
        t.setBody("Your claim has been submitted.");

        assertEquals(1, t.getId());
        assertEquals("CLAIM_SUBMITTED", t.getName());
        assertEquals("SMS", t.getChannel());
        assertTrue(t.isActive());
        assertEquals("Your claim has been submitted.", t.getBody());
    }

    @Test
    void activeDefaultsFalse() {
        NotificationTemplate t = new NotificationTemplate();
        assertFalse(t.isActive());
    }
}

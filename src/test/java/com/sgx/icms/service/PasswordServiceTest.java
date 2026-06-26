package com.sgx.icms.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordServiceTest {

    private final PasswordService svc = new PasswordService();

    @Test
    void hashRejectsNullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> svc.hash(null));
        assertThrows(IllegalArgumentException.class, () -> svc.hash(""));
    }

    @Test
    void hashProducesValueDifferentFromRawPassword() {
        String hash = svc.hash("S3cret!");
        assertNotEquals("S3cret!", hash);
        assertTrue(hash.startsWith("$2"));
    }

    @Test
    void matchesReturnsTrueForCorrectPassword() {
        String hash = svc.hash("S3cret!");
        assertTrue(svc.matches("S3cret!", hash));
    }

    @Test
    void matchesReturnsFalseForWrongPassword() {
        String hash = svc.hash("S3cret!");
        assertFalse(svc.matches("wrong", hash));
    }

    @Test
    void matchesReturnsFalseForNullInputs() {
        assertFalse(svc.matches(null, "hash"));
        assertFalse(svc.matches("pw", null));
        assertFalse(svc.matches("pw", ""));
    }

    @Test
    void matchesReturnsFalseForMalformedHashInsteadOfThrowing() {
        assertFalse(svc.matches("pw", "not-a-real-bcrypt-hash"));
    }
}

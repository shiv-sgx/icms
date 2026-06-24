package com.sgx.icms.service;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt password hashing/verification. Centralised so the work factor and the
 * library choice live in exactly one place (used by login and by admin
 * create-user / reset-password).
 */
public class PasswordService {

    /** Cost factor: 2^10 rounds — a sensible default for an on-prem demo system. */
    private static final int LOG_ROUNDS = 10;

    public String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /** Constant-time-ish verification; never throws on malformed input. */
    public boolean matches(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, storedHash);
        } catch (IllegalArgumentException e) {
            // Malformed/legacy hash — treat as non-match rather than leaking a 500.
            return false;
        }
    }
}

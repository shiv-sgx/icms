package com.sgx.icms.web.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.sgx.icms.domain.User;

class SessionUserTest {

    @Test
    void constructorAndGetters() {
        SessionUser su = new SessionUser(1L, "jdoe", "John Doe", "john@example.com", "AGENT", "HQ");

        assertEquals(1L, su.getId());
        assertEquals("jdoe", su.getUsername());
        assertEquals("John Doe", su.getFullName());
        assertEquals("john@example.com", su.getEmail());
        assertEquals("AGENT", su.getRole());
        assertEquals("HQ", su.getBranch());
    }

    @Test
    void fromUserCopiesFieldsButNotPasswordHash() {
        User u = new User();
        u.setId(2L);
        u.setUsername("admin");
        u.setFullName("Jane Admin");
        u.setEmail("jane@example.com");
        u.setRoleName("ADMIN");
        u.setBranch("Branch1");
        u.setPasswordHash("secret-hash");

        SessionUser su = SessionUser.from(u);

        assertEquals(2L, su.getId());
        assertEquals("admin", su.getUsername());
        assertEquals("Jane Admin", su.getFullName());
        assertEquals("jane@example.com", su.getEmail());
        assertEquals("ADMIN", su.getRole());
        assertEquals("Branch1", su.getBranch());
    }

    @Test
    void hasRoleIsCaseInsensitive() {
        SessionUser su = new SessionUser(1L, "u", "U", "u@e.com", "Agent", "HQ");
        assertTrue(su.hasRole("AGENT"));
        assertTrue(su.hasRole("agent"));
    }

    @Test
    void hasRoleFalseForOtherRole() {
        SessionUser su = new SessionUser(1L, "u", "U", "u@e.com", "AGENT", "HQ");
        assertFalse(su.hasRole("MANAGER"));
    }

    @Test
    void hasRoleFalseWhenRoleIsNull() {
        SessionUser su = new SessionUser(1L, "u", "U", "u@e.com", null, "HQ");
        assertFalse(su.hasRole("AGENT"));
    }

    @Test
    void sessionKeyConstantIsStable() {
        assertEquals("ICMS_USER", SessionUser.SESSION_KEY);
    }
}

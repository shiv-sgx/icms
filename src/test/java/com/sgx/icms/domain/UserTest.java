package com.sgx.icms.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void gettersAndSettersRoundTrip() {
        User u = new User();
        LocalDateTime lastLogin = LocalDateTime.of(2026, 1, 1, 8, 0);
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 8, 0);

        u.setId(1L);
        u.setFullName("Jane Admin");
        u.setEmail("jane@example.com");
        u.setUsername("admin");
        u.setPasswordHash("hashed");
        u.setRoleId(5);
        u.setRoleName("ADMIN");
        u.setBranch("HQ");
        u.setStatus("ACTIVE");
        u.setLastLogin(lastLogin);
        u.setCreatedAt(createdAt);

        assertEquals(1L, u.getId());
        assertEquals("Jane Admin", u.getFullName());
        assertEquals("jane@example.com", u.getEmail());
        assertEquals("admin", u.getUsername());
        assertEquals("hashed", u.getPasswordHash());
        assertEquals(5, u.getRoleId());
        assertEquals("ADMIN", u.getRoleName());
        assertEquals("HQ", u.getBranch());
        assertEquals("ACTIVE", u.getStatus());
        assertEquals(lastLogin, u.getLastLogin());
        assertEquals(createdAt, u.getCreatedAt());
    }

    @Test
    void isActiveTrueCaseInsensitive() {
        User u = new User();
        u.setStatus("active");
        assertTrue(u.isActive());
    }

    @Test
    void isActiveFalseForOtherStatuses() {
        User u = new User();
        u.setStatus("INACTIVE");
        assertFalse(u.isActive());
    }

    @Test
    void isActiveFalseWhenStatusNull() {
        User u = new User();
        assertFalse(u.isActive());
    }
}

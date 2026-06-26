package com.sgx.icms.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RolesTest {

    @Test
    void allContainsExactlyTheFiveRoles() {
        assertEquals(5, Roles.ALL.size());
        assertTrue(Roles.ALL.containsAll(java.util.Arrays.asList(
                Roles.CUSTOMER, Roles.AGENT, Roles.SURVEYOR, Roles.MANAGER, Roles.ADMIN)));
    }

    @Test
    void forNamespaceMatchesExactPrefix() {
        assertEquals(Roles.CUSTOMER, Roles.forNamespace("/customer"));
        assertEquals(Roles.AGENT, Roles.forNamespace("/agent"));
        assertEquals(Roles.SURVEYOR, Roles.forNamespace("/surveyor"));
        assertEquals(Roles.MANAGER, Roles.forNamespace("/manager"));
        assertEquals(Roles.ADMIN, Roles.forNamespace("/admin"));
    }

    @Test
    void forNamespaceMatchesSubPath() {
        assertEquals(Roles.ADMIN, Roles.forNamespace("/admin/config"));
        assertEquals(Roles.AGENT, Roles.forNamespace("/agent/claims/123"));
    }

    @Test
    void forNamespaceDoesNotMatchUnrelatedPrefix() {
        assertNull(Roles.forNamespace("/administration"));
        assertNull(Roles.forNamespace("/unknown"));
    }

    @Test
    void forNamespaceReturnsNullForNull() {
        assertNull(Roles.forNamespace(null));
    }

    @Test
    void dashboardForReturnsRoleSpecificPath() {
        assertEquals("/customer/dashboard", Roles.dashboardFor(Roles.CUSTOMER));
        assertEquals("/agent/dashboard", Roles.dashboardFor(Roles.AGENT));
        assertEquals("/surveyor/dashboard", Roles.dashboardFor(Roles.SURVEYOR));
        assertEquals("/manager/dashboard", Roles.dashboardFor(Roles.MANAGER));
        assertEquals("/admin/dashboard", Roles.dashboardFor(Roles.ADMIN));
    }

    @Test
    void dashboardForReturnsRootForNullOrUnknownRole() {
        assertEquals("/", Roles.dashboardFor(null));
        assertEquals("/", Roles.dashboardFor("UNKNOWN"));
    }
}

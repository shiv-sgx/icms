package com.sgx.icms.web.action.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.opensymphony.xwork2.Action;
import com.sgx.icms.domain.SlaConfig;
import com.sgx.icms.service.AdminService;
import com.sgx.icms.web.support.SessionUser;

class ConfigActionTest {

    private static Map<String, Object> sessionWith(SessionUser u) {
        Map<String, Object> session = new HashMap<>();
        session.put(SessionUser.SESSION_KEY, u);
        return session;
    }

    @Test
    void sla_loadsListAndConsumesFlash() {
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class)) {
            ConfigAction action = new ConfigAction();
            action.setSession(new HashMap<>());
            AdminService svc = mocked.constructed().get(0);
            java.util.List<SlaConfig> list = java.util.Collections.singletonList(new SlaConfig());
            org.mockito.Mockito.when(svc.slaConfigs()).thenReturn(list);

            assertEquals(Action.SUCCESS, action.sla());
            assertSame(list, action.getSlaList());
        }
    }

    @Test
    void updateSla_success_setsFlashAndRedirect() {
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class)) {
            ConfigAction action = new ConfigAction();
            action.setSession(sessionWith(new SessionUser(1, "admin", "Admin", "a@x.com", "ADMIN", "HQ")));
            action.setId(5);
            action.setHours(48);

            assertEquals(Action.SUCCESS, action.updateSla());
            assertEquals("/admin/sla", action.getRedirectUrl());
            assertEquals("success", action.getFlashType());
        }
    }

    @Test
    void updateSla_serviceThrows_setsErrorFlash() {
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class,
                (mock, ctx) -> doThrow(new IllegalStateException("nope"))
                        .when(mock).updateSla(any(), anyInt(), anyInt(), any()))) {
            ConfigAction action = new ConfigAction();
            action.setSession(sessionWith(new SessionUser(1, "admin", "Admin", "a@x.com", "ADMIN", "HQ")));

            assertEquals(Action.SUCCESS, action.updateSla());
            assertEquals("error", action.getFlashType());
            assertEquals("nope", action.getFlashMessage());
        }
    }

    @Test
    void updateThreshold_validAmounts_callsService() {
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class)) {
            ConfigAction action = new ConfigAction();
            action.setSession(sessionWith(new SessionUser(1, "admin", "Admin", "a@x.com", "ADMIN", "HQ")));
            action.setId(2);
            action.setMinAmount("1000");
            action.setMaxAmount("5000");

            assertEquals(Action.SUCCESS, action.updateThreshold());
            AdminService svc = mocked.constructed().get(0);
            org.mockito.Mockito.verify(svc).updateThreshold(any(), eq(2), eq(new BigDecimal("1000")),
                    eq(new BigDecimal("5000")), any());
            assertEquals("success", action.getFlashType());
        }
    }

    @Test
    void updateThreshold_blankMax_isTreatedAsNull() {
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class)) {
            ConfigAction action = new ConfigAction();
            action.setSession(sessionWith(new SessionUser(1, "admin", "Admin", "a@x.com", "ADMIN", "HQ")));
            action.setMinAmount("100");
            action.setMaxAmount("  ");

            assertEquals(Action.SUCCESS, action.updateThreshold());
            AdminService svc = mocked.constructed().get(0);
            org.mockito.Mockito.verify(svc).updateThreshold(any(), anyInt(), eq(new BigDecimal("100")),
                    eq(null), any());
        }
    }

    @Test
    void updateThreshold_nullMinAmount_setsErrorFlash() {
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class)) {
            ConfigAction action = new ConfigAction();
            action.setSession(new HashMap<>());
            action.setMinAmount(null);

            assertEquals(Action.SUCCESS, action.updateThreshold());
            assertEquals("error", action.getFlashType());
            assertEquals("Enter valid amounts.", action.getFlashMessage());
        }
    }

    @Test
    void updateThreshold_nonNumericAmount_setsErrorFlash() {
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class)) {
            ConfigAction action = new ConfigAction();
            action.setSession(new HashMap<>());
            action.setMinAmount("not-a-number");

            assertEquals(Action.SUCCESS, action.updateThreshold());
            assertEquals("error", action.getFlashType());
        }
    }

    @Test
    void templates_loadsList() {
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class)) {
            ConfigAction action = new ConfigAction();
            action.setSession(new HashMap<>());
            assertEquals(Action.SUCCESS, action.templates());
        }
    }

    @Test
    void updateTemplate_success() {
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class)) {
            ConfigAction action = new ConfigAction();
            action.setSession(sessionWith(new SessionUser(1, "admin", "Admin", "a@x.com", "ADMIN", "HQ")));
            action.setActive(true);
            action.setBody("hello");

            assertEquals(Action.SUCCESS, action.updateTemplate());
            assertEquals("/admin/templates", action.getRedirectUrl());
            assertEquals("success", action.getFlashType());
        }
    }

    @Test
    void documents_loadsList() {
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class)) {
            ConfigAction action = new ConfigAction();
            action.setSession(new HashMap<>());
            assertEquals(Action.SUCCESS, action.documents());
        }
    }

    @Test
    void addDocument_success_trimsFields() {
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class)) {
            ConfigAction action = new ConfigAction();
            action.setSession(sessionWith(new SessionUser(1, "admin", "Admin", "a@x.com", "ADMIN", "HQ")));
            action.setClaimType(" MOTOR ");
            action.setClaimSubtype("  ");
            action.setDocType(" RC ");
            action.setRequired(true);

            assertEquals(Action.SUCCESS, action.addDocument());
            assertEquals("/admin/documents", action.getRedirectUrl());
            assertEquals("success", action.getFlashType());
        }
    }

    @Test
    void addDocument_serviceThrows_setsErrorFlash() {
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class,
                (mock, ctx) -> doThrow(new IllegalArgumentException("Claim type and document type are required."))
                        .when(mock).addDocumentRequirement(any(), any(), any()))) {
            ConfigAction action = new ConfigAction();
            action.setSession(sessionWith(new SessionUser(1, "admin", "Admin", "a@x.com", "ADMIN", "HQ")));

            assertEquals(Action.SUCCESS, action.addDocument());
            assertEquals("error", action.getFlashType());
        }
    }

    @Test
    void deleteDocument_success() {
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class)) {
            ConfigAction action = new ConfigAction();
            action.setSession(sessionWith(new SessionUser(1, "admin", "Admin", "a@x.com", "ADMIN", "HQ")));
            action.setId(9);

            assertEquals(Action.SUCCESS, action.deleteDocument());
            assertEquals("/admin/documents", action.getRedirectUrl());
            assertEquals("success", action.getFlashType());
        }
    }
}

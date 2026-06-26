package com.sgx.icms.web.action.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.opensymphony.xwork2.Action;
import com.sgx.icms.domain.AuditLog;
import com.sgx.icms.service.AdminService;
import com.sgx.icms.web.support.Paged;

class AuditActionTest {

    @Test
    void execute_blankFilters_areNormalizedToNull() {
        Paged<AuditLog> page = new Paged<>(java.util.Collections.emptyList(), 1, 15, 0);
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class)) {
            AuditAction action = new AuditAction();
            AdminService svc = mocked.constructed().get(0);
            when(svc.auditLogs(isNull(), isNull(), eq(1), eq(15))).thenReturn(page);

            action.setActionName("   ");
            action.setResult("");
            action.setPage(0);

            String result = action.execute();

            assertEquals(Action.SUCCESS, result);
            assertSame(page, action.getLogs());
            assertEquals(1, action.getPage());
        }
    }

    @Test
    void execute_passesThroughTrimmedFiltersAndPage() {
        Paged<AuditLog> page = new Paged<>(java.util.Collections.emptyList(), 3, 15, 0);
        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class)) {
            AuditAction action = new AuditAction();
            AdminService svc = mocked.constructed().get(0);
            when(svc.auditLogs(eq("LOGIN"), eq("FAILED"), eq(3), eq(15))).thenReturn(page);

            action.setActionName("  LOGIN  ");
            action.setResult("FAILED");
            action.setPage(3);

            assertEquals(Action.SUCCESS, action.execute());
            assertSame(page, action.getLogs());
        }
    }
}

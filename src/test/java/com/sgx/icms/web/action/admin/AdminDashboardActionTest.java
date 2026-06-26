package com.sgx.icms.web.action.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.opensymphony.xwork2.Action;
import com.sgx.icms.service.AdminService;
import com.sgx.icms.web.support.AdminStats;

class AdminDashboardActionTest {

    @Test
    void execute_populatesStatsFromAdminService() {
        AdminStats stats = new AdminStats();
        stats.setUsers(42);

        try (MockedConstruction<AdminService> mocked = mockConstruction(AdminService.class)) {
            AdminDashboardAction action = new AdminDashboardAction();
            AdminService svc = mocked.constructed().get(0);
            when(svc.stats()).thenReturn(stats);

            String result = action.execute();

            assertEquals(Action.SUCCESS, result);
            assertSame(stats, action.getStats());
        }
    }
}

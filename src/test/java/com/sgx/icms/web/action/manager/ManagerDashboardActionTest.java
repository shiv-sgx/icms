package com.sgx.icms.web.action.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.opensymphony.xwork2.Action;
import com.sgx.icms.domain.Claim;
import com.sgx.icms.service.AgentClaimService;
import com.sgx.icms.service.ManagerService;
import com.sgx.icms.service.ReportService;
import com.sgx.icms.web.support.Paged;
import com.sgx.icms.web.support.ReportTable;

class ManagerDashboardActionTest {

    @Test
    void execute_aggregatesStatsQueueAndReport() {
        long[] stats = {3, 2, 1, 10};
        Paged<Claim> page = new Paged<>(Collections.singletonList(new Claim()), 1, 8, 1);
        ReportTable table = new ReportTable("agent-performance", "Agent Performance",
                Collections.singletonList("Agent"), Collections.emptyList());

        try (MockedConstruction<ManagerService> managerMock = mockConstruction(ManagerService.class);
             MockedConstruction<ReportService> reportMock = mockConstruction(ReportService.class);
             MockedConstruction<AgentClaimService> claimsMock = mockConstruction(AgentClaimService.class)) {

            ManagerDashboardAction action = new ManagerDashboardAction();
            when(managerMock.constructed().get(0).dashboardStats()).thenReturn(stats);
            when(claimsMock.constructed().get(0).list("PENDING_APPROVAL", null, null, 1, 8)).thenReturn(page);
            when(reportMock.constructed().get(0).report("agent-performance")).thenReturn(table);

            assertEquals(Action.SUCCESS, action.execute());
            assertEquals(3, action.getPendingApproval());
            assertEquals(2, action.getHighRisk());
            assertEquals(1, action.getSlaBreaches());
            assertEquals(10, action.getSettled());
            assertEquals(1, action.getQueue().size());
            assertSame(table, action.getAgentPerformance());
        }
    }
}

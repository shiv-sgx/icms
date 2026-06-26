package com.sgx.icms.web.action.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.sgx.icms.domain.ClaimStatus;
import com.sgx.icms.service.AgentClaimService;
import com.sgx.icms.web.action.BaseAction;

class AgentDashboardActionTest {

    @Test
    void execute_computesKpisFromStatusCounts() {
        try (MockedConstruction<AgentClaimService> mocked = mockConstruction(AgentClaimService.class)) {
            Map<String, Long> counts = new LinkedHashMap<>();
            counts.put(ClaimStatus.SUBMITTED, 4L);
            counts.put(ClaimStatus.SURVEY_SCHEDULED, 2L);
            counts.put(ClaimStatus.PENDING_APPROVAL, 1L);
            counts.put(ClaimStatus.SETTLED, 3L);
            counts.put(ClaimStatus.CLOSED, 1L);
            counts.put(ClaimStatus.REJECTED, 1L);
            counts.put(ClaimStatus.WITHDRAWN, 1L);
            AgentClaimService svc = mocked.constructed().get(0);
            when(svc.statusCounts()).thenReturn(counts);
            when(svc.worklist(10)).thenReturn(Collections.emptyList());

            AgentDashboardAction action = new AgentDashboardAction();

            assertEquals(BaseAction.SUCCESS, action.execute());
            // total=13, terminal = settled(3)+closed(1)+rejected(1)+withdrawn(1) = 6 -> open = 7
            assertEquals(7L, action.getOpenClaims());
            assertEquals(2L, action.getAwaitingSurvey());
            assertEquals(1L, action.getPendingApproval());
            assertEquals(4L, action.getSettled());
        }
    }

    @Test
    void execute_emptyCounts_allZero() {
        try (MockedConstruction<AgentClaimService> mocked = mockConstruction(AgentClaimService.class)) {
            AgentClaimService svc = mocked.constructed().get(0);
            when(svc.statusCounts()).thenReturn(Collections.emptyMap());
            when(svc.worklist(10)).thenReturn(Collections.emptyList());

            AgentDashboardAction action = new AgentDashboardAction();

            assertEquals(BaseAction.SUCCESS, action.execute());
            assertEquals(0L, action.getOpenClaims());
            assertEquals(0L, action.getSettled());
        }
    }
}

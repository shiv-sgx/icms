package com.sgx.icms.web.action.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.sgx.icms.domain.Claim;
import com.sgx.icms.service.AgentClaimService;
import com.sgx.icms.web.action.BaseAction;
import com.sgx.icms.web.support.Paged;

class AgentClaimListActionTest {

    @Test
    void execute_blankFilters_passedAsNull() {
        try (MockedConstruction<AgentClaimService> mocked = mockConstruction(AgentClaimService.class)) {
            Paged<Claim> page = new Paged<>(Collections.emptyList(), 1, 15, 0);
            when(mocked.constructed().get(0).list(isNull(), isNull(), isNull(), eq(1), eq(15)))
                    .thenReturn(page);

            AgentClaimListAction action = new AgentClaimListAction();
            action.setStatus("  ");
            action.setType("");
            action.setQ(null);
            action.setPage(1);

            assertEquals(BaseAction.SUCCESS, action.execute());
            assertEquals(page, action.getClaims());
        }
    }

    @Test
    void execute_withFilters_trimsAndPasses() {
        try (MockedConstruction<AgentClaimService> mocked = mockConstruction(AgentClaimService.class)) {
            Paged<Claim> page = new Paged<>(Collections.emptyList(), 1, 15, 0);
            when(mocked.constructed().get(0).list(eq("SUBMITTED"), eq("AUTO"), eq("CLM"), eq(2), eq(15)))
                    .thenReturn(page);

            AgentClaimListAction action = new AgentClaimListAction();
            action.setStatus(" SUBMITTED ");
            action.setType(" AUTO ");
            action.setQ(" CLM ");
            action.setPage(2);

            assertEquals(BaseAction.SUCCESS, action.execute());
            assertEquals("SUBMITTED", action.getStatus());
        }
    }
}

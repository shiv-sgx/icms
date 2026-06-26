package com.sgx.icms.web.action.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.sgx.icms.domain.Claim;
import com.sgx.icms.service.AgentClaimService;
import com.sgx.icms.service.AssignmentService;
import com.sgx.icms.web.action.BaseAction;
import com.sgx.icms.web.support.ClaimBundle;

class AgentClaimDetailActionTest {

    @Test
    void execute_claimNotFound_returnsMissing() {
        try (MockedConstruction<AgentClaimService> agentSvc = mockConstruction(AgentClaimService.class);
             MockedConstruction<AssignmentService> assignSvc = mockConstruction(AssignmentService.class)) {

            when(agentSvc.constructed().get(0).bundle(eq(7L))).thenReturn(null);

            AgentClaimDetailAction action = new AgentClaimDetailAction();
            action.setSession(new HashMap<>());
            action.setId(7L);

            assertEquals("missing", action.execute());
        }
    }

    @Test
    void execute_claimFound_loadsSurveyorsAndFlash() {
        try (MockedConstruction<AgentClaimService> agentSvc = mockConstruction(AgentClaimService.class);
             MockedConstruction<AssignmentService> assignSvc = mockConstruction(AssignmentService.class)) {

            ClaimBundle bundle = new ClaimBundle();
            bundle.setClaim(new Claim());
            when(agentSvc.constructed().get(0).bundle(eq(7L))).thenReturn(bundle);
            when(assignSvc.constructed().get(0).availableSurveyors()).thenReturn(Collections.emptyList());

            AgentClaimDetailAction action = new AgentClaimDetailAction();
            action.setSession(new HashMap<>());
            action.setId(7L);

            assertEquals(BaseAction.SUCCESS, action.execute());
            assertEquals(bundle, action.getBundle());
        }
    }
}

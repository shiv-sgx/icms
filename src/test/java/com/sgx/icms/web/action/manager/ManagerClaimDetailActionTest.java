package com.sgx.icms.web.action.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.sgx.icms.service.AgentClaimService;
import com.sgx.icms.web.support.ClaimBundle;

class ManagerClaimDetailActionTest {

    @Test
    void execute_claimFound_returnsSuccess() {
        ClaimBundle bundle = new ClaimBundle();
        try (MockedConstruction<AgentClaimService> mocked = mockConstruction(AgentClaimService.class)) {
            ManagerClaimDetailAction action = new ManagerClaimDetailAction();
            action.setSession(new HashMap<>());
            AgentClaimService svc = mocked.constructed().get(0);
            when(svc.bundle(7L)).thenReturn(bundle);

            action.setId(7);

            assertEquals("success", action.execute());
            assertSame(bundle, action.getBundle());
        }
    }

    @Test
    void execute_claimMissing_returnsMissingAndFlash() {
        try (MockedConstruction<AgentClaimService> mocked = mockConstruction(AgentClaimService.class)) {
            ManagerClaimDetailAction action = new ManagerClaimDetailAction();
            action.setSession(new HashMap<>());
            AgentClaimService svc = mocked.constructed().get(0);
            when(svc.bundle(7L)).thenReturn(null);

            action.setId(7);

            assertEquals("missing", action.execute());
        }
    }
}

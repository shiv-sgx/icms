package com.sgx.icms.web.action.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.opensymphony.xwork2.Action;
import com.sgx.icms.domain.Claim;
import com.sgx.icms.service.AgentClaimService;
import com.sgx.icms.web.support.Paged;

class ApprovalQueueActionTest {

    @Test
    void execute_listsPendingApprovalClaims() {
        Paged<Claim> page = new Paged<>(Collections.emptyList(), 2, 15, 0);
        try (MockedConstruction<AgentClaimService> mocked = mockConstruction(AgentClaimService.class)) {
            ApprovalQueueAction action = new ApprovalQueueAction();
            AgentClaimService svc = mocked.constructed().get(0);
            when(svc.list("PENDING_APPROVAL", null, null, 2, 15)).thenReturn(page);

            action.setPage(2);

            assertEquals(Action.SUCCESS, action.execute());
            assertSame(page, action.getClaimsPage());
            verify(svc).list("PENDING_APPROVAL", null, null, 2, 15);
        }
    }
}

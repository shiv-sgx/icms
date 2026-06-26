package com.sgx.icms.web.action.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockConstruction;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.sgx.icms.service.AgentClaimService;

class AgentBaseActionTest {

    static class TestAction extends AgentBaseAction {
        private static final long serialVersionUID = 1L;
        @Override
        public String execute() { return SUCCESS; }
        String detailUrlPublic(long id) { return detailUrl(id); }
    }

    @Test
    void detailUrl_buildsAgentClaimPath() {
        try (MockedConstruction<AgentClaimService> mocked = mockConstruction(AgentClaimService.class)) {
            TestAction action = new TestAction();
            assertEquals("/agent/claim?id=42", action.detailUrlPublic(42L));
        }
    }
}

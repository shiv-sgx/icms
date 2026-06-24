package com.sgx.icms.web.action.agent;

import com.sgx.icms.service.AgentClaimService;
import com.sgx.icms.web.action.BaseAction;

/** Shared base for agent-portal actions. */
public abstract class AgentBaseAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    protected final transient AgentClaimService agentClaims = new AgentClaimService();

    protected String detailUrl(long claimId) {
        return "/agent/claim?id=" + claimId;
    }
}

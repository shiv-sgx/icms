package com.sgx.icms.web.action.manager;

import com.sgx.icms.service.AgentClaimService;
import com.sgx.icms.web.action.BaseAction;

/** Shared base for manager-portal actions. */
public abstract class ManagerBaseAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    protected final transient AgentClaimService claims = new AgentClaimService();

    protected String detailUrl(long claimId) {
        return "/manager/claim?id=" + claimId;
    }
}

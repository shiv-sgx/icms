package com.sgx.icms.web.action.customer;

import java.util.Collections;
import java.util.List;

import com.sgx.icms.domain.Policy;

/** Customer profile: account details, linked policyholder, and their policies. */
public class ProfileAction extends CustomerBaseAction {

    private static final long serialVersionUID = 1L;

    private transient List<Policy> policies = Collections.emptyList();

    @Override
    public String execute() {
        if (policyholder() != null) {
            policies = claimService.policiesForCustomer(policyholder().getId());
        }
        return SUCCESS;
    }

    public List<Policy> getPolicies() { return policies; }
}

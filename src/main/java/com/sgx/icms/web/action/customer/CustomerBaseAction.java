package com.sgx.icms.web.action.customer;

import com.sgx.icms.domain.Policyholder;
import com.sgx.icms.service.ClaimService;
import com.sgx.icms.web.action.BaseAction;

/**
 * Base for customer-portal actions: lazily resolves the logged-in customer's
 * policyholder record (linked by email) and shares the {@link ClaimService}.
 */
public abstract class CustomerBaseAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    protected final transient ClaimService claimService = new ClaimService();

    private transient Policyholder policyholder;
    private boolean resolved;

    /** The customer's policyholder, or {@code null} if no profile is linked. */
    protected Policyholder policyholder() {
        if (!resolved) {
            String email = currentUser() == null ? null : currentUser().getEmail();
            policyholder = (email == null) ? null : claimService.resolveCustomer(email);
            resolved = true;
        }
        return policyholder;
    }

    /** Exposed to JSPs. */
    public Policyholder getPolicyholder() {
        return policyholder();
    }

    public boolean isHasProfile() {
        return policyholder() != null;
    }
}

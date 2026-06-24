package com.sgx.icms.web.action.customer;

import com.sgx.icms.domain.Claim;
import com.sgx.icms.web.support.Paged;

/** Paginated list of the customer's own claims. */
public class ClaimListAction extends CustomerBaseAction {

    private static final long serialVersionUID = 1L;

    private int page = 1;
    private transient Paged<Claim> claims;

    @Override
    public String execute() {
        if (policyholder() != null) {
            claims = claimService.listForCustomer(policyholder().getId(), normalizePage(page), defaultPageSize());
        }
        return SUCCESS;
    }

    public void setPage(int page) { this.page = page; }
    public int getPage() { return page; }

    public Paged<Claim> getClaims() { return claims; }
}

package com.sgx.icms.web.action.manager;

import com.sgx.icms.domain.Claim;
import com.sgx.icms.web.support.Paged;

/** Paginated queue of claims awaiting approval. */
public class ApprovalQueueAction extends ManagerBaseAction {

    private static final long serialVersionUID = 1L;

    private int page = 1;
    private transient Paged<Claim> claimsPage;

    @Override
    public String execute() {
        claimsPage = claims.list("PENDING_APPROVAL", null, null, normalizePage(page), defaultPageSize());
        return SUCCESS;
    }

    public void setPage(int page) { this.page = page; }
    public int getPage() { return page; }
    public Paged<Claim> getClaimsPage() { return claimsPage; }
}

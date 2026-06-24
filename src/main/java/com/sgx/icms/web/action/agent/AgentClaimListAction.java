package com.sgx.icms.web.action.agent;

import com.sgx.icms.domain.Claim;
import com.sgx.icms.web.support.Paged;

/** Filterable, paginated claims list for agents. */
public class AgentClaimListAction extends AgentBaseAction {

    private static final long serialVersionUID = 1L;

    private String status;
    private String type;
    private String q;
    private int page = 1;
    private transient Paged<Claim> claims;

    @Override
    public String execute() {
        claims = agentClaims.list(trim(status), trim(type), trim(q), normalizePage(page), defaultPageSize());
        return SUCCESS;
    }

    private static String trim(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }
    public void setType(String type) { this.type = type; }
    public String getType() { return type; }
    public void setQ(String q) { this.q = q; }
    public String getQ() { return q; }
    public void setPage(int page) { this.page = page; }
    public int getPage() { return page; }
    public Paged<Claim> getClaims() { return claims; }
}

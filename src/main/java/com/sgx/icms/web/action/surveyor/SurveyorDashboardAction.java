package com.sgx.icms.web.action.surveyor;

import com.sgx.icms.domain.Claim;
import com.sgx.icms.web.support.Paged;

/** Surveyor dashboard: assigned-claim KPIs and list. */
public class SurveyorDashboardAction extends SurveyorBaseAction {

    private static final long serialVersionUID = 1L;

    private long totalAssigned;
    private long pendingSurvey;
    private long assessed;
    private int page = 1;
    private transient Paged<Claim> claims;

    @Override
    public String execute() {
        long sid = currentUser().getId();
        long[] counts = surveyorService.counts(sid);
        totalAssigned = counts[0];
        pendingSurvey = counts[1];
        assessed = counts[2];
        claims = surveyorService.assignedClaims(sid, normalizePage(page), defaultPageSize());
        return SUCCESS;
    }

    public long getTotalAssigned() { return totalAssigned; }
    public long getPendingSurvey() { return pendingSurvey; }
    public long getAssessed() { return assessed; }
    public void setPage(int page) { this.page = page; }
    public int getPage() { return page; }
    public Paged<Claim> getClaims() { return claims; }
}

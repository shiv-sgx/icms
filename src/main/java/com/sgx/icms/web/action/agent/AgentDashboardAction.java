package com.sgx.icms.web.action.agent;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.ClaimStatus;

/** Agent dashboard: status KPIs and the action worklist. */
public class AgentDashboardAction extends AgentBaseAction {

    private static final long serialVersionUID = 1L;

    private long openClaims;
    private long awaitingSurvey;
    private long pendingApproval;
    private long settled;
    private List<Claim> worklist = Collections.emptyList();

    @Override
    public String execute() {
        Map<String, Long> counts = agentClaims.statusCounts();
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        long terminal = c(counts, ClaimStatus.SETTLED) + c(counts, ClaimStatus.CLOSED)
                + c(counts, ClaimStatus.REJECTED) + c(counts, ClaimStatus.WITHDRAWN);
        openClaims = total - terminal;
        awaitingSurvey = c(counts, ClaimStatus.SURVEY_SCHEDULED);
        pendingApproval = c(counts, ClaimStatus.PENDING_APPROVAL);
        settled = c(counts, ClaimStatus.SETTLED) + c(counts, ClaimStatus.CLOSED);
        worklist = agentClaims.worklist(10);
        return SUCCESS;
    }

    private static long c(Map<String, Long> m, String k) {
        return m.getOrDefault(k, 0L);
    }

    public long getOpenClaims() { return openClaims; }
    public long getAwaitingSurvey() { return awaitingSurvey; }
    public long getPendingApproval() { return pendingApproval; }
    public long getSettled() { return settled; }
    public List<Claim> getWorklist() { return worklist; }
}

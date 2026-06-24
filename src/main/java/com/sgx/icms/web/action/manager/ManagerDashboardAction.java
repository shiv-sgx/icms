package com.sgx.icms.web.action.manager;

import java.util.Collections;
import java.util.List;

import com.sgx.icms.domain.Claim;
import com.sgx.icms.service.ManagerService;
import com.sgx.icms.service.ReportService;
import com.sgx.icms.web.support.ReportTable;

/** Manager dashboard: approval/risk KPIs, approval queue preview, agent performance. */
public class ManagerDashboardAction extends ManagerBaseAction {

    private static final long serialVersionUID = 1L;

    private final transient ManagerService managerService = new ManagerService();
    private final transient ReportService reportService = new ReportService();

    private long pendingApproval;
    private long highRisk;
    private long slaBreaches;
    private long settled;
    private List<Claim> queue = Collections.emptyList();
    private transient ReportTable agentPerformance;

    @Override
    public String execute() {
        long[] stats = managerService.dashboardStats();
        pendingApproval = stats[0];
        highRisk = stats[1];
        slaBreaches = stats[2];
        settled = stats[3];
        queue = claims.list("PENDING_APPROVAL", null, null, 1, 8).getItems();
        agentPerformance = reportService.report("agent-performance");
        return SUCCESS;
    }

    public long getPendingApproval() { return pendingApproval; }
    public long getHighRisk() { return highRisk; }
    public long getSlaBreaches() { return slaBreaches; }
    public long getSettled() { return settled; }
    public List<Claim> getQueue() { return queue; }
    public ReportTable getAgentPerformance() { return agentPerformance; }
}

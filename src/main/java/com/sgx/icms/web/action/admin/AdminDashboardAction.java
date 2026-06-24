package com.sgx.icms.web.action.admin;

import com.sgx.icms.web.support.AdminStats;

/** Admin dashboard: system counts + live HikariCP pool stats. */
public class AdminDashboardAction extends AdminBaseAction {

    private static final long serialVersionUID = 1L;

    private transient AdminStats stats;

    @Override
    public String execute() {
        stats = adminService.stats();
        return SUCCESS;
    }

    public AdminStats getStats() { return stats; }
}

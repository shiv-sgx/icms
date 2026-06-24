package com.sgx.icms.web.action.admin;

import com.sgx.icms.domain.AuditLog;
import com.sgx.icms.web.support.Paged;

/** Audit log viewer with action/result filters and pagination. */
public class AuditAction extends AdminBaseAction {

    private static final long serialVersionUID = 1L;

    private String actionName;
    private String result;
    private int page = 1;
    private transient Paged<AuditLog> logs;

    @Override
    public String execute() {
        logs = adminService.auditLogs(trim(actionName), trim(result), normalizePage(page), defaultPageSize());
        return SUCCESS;
    }

    private static String trim(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    public void setActionName(String actionName) { this.actionName = actionName; }
    public String getActionName() { return actionName; }
    public void setResult(String result) { this.result = result; }
    public String getResult() { return result; }
    public void setPage(int page) { this.page = page; }
    public int getPage() { return page; }
    public Paged<AuditLog> getLogs() { return logs; }
}

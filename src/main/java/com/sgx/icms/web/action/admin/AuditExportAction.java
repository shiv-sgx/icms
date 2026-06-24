package com.sgx.icms.web.action.admin;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sgx.icms.domain.AuditLog;
import com.sgx.icms.web.support.CsvWriter;
import com.sgx.icms.web.support.ReportTable;

/** Streams the (optionally filtered) audit log as a CSV download. */
public class AuditExportAction extends AdminBaseAction {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(AuditExportAction.class);

    private String actionName;
    private String result;

    @Override
    public String execute() {
        List<AuditLog> entries = adminService.auditLogsForExport(trim(actionName), trim(result));
        List<List<String>> rows = new ArrayList<>();
        for (AuditLog a : entries) {
            rows.add(Arrays.asList(
                    str(a.getTs()), nz(a.getUsername()), nz(a.getRole()), nz(a.getAction()),
                    nz(a.getEntity()), nz(a.getIpAddress()), nz(a.getResult())));
        }
        ReportTable table = new ReportTable("audit-log", "Audit Log",
                Arrays.asList("Timestamp", "User", "Role", "Action", "Entity", "IP", "Result"), rows);

        HttpServletResponse response = ServletActionContext.getResponse();
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"audit-log.csv\"");
        try (PrintWriter out = response.getWriter()) {
            out.write(CsvWriter.toCsv(table));
            out.flush();
        } catch (Exception e) {
            LOG.error("Failed to export audit log", e);
        }
        new com.sgx.icms.service.AuditService()
                .success(currentUser(), "AUDIT_EXPORT", "audit-log", clientIp());
        return NONE;
    }

    private static String trim(String s) { return (s == null || s.trim().isEmpty()) ? null : s.trim(); }
    private static String nz(String s) { return s == null ? "" : s; }
    private static String str(Object o) { return o == null ? "" : o.toString(); }

    public void setActionName(String actionName) { this.actionName = actionName; }
    public String getActionName() { return actionName; }
    public void setResult(String result) { this.result = result; }
    public String getResult() { return result; }
}

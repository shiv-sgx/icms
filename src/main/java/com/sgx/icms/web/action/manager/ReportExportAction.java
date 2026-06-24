package com.sgx.icms.web.action.manager;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sgx.icms.service.ReportService;
import com.sgx.icms.web.support.CsvWriter;
import com.sgx.icms.web.support.ReportTable;

/**
 * Streams a single report as a CSV download. Writes directly to the servlet
 * response and returns NONE (no Struts result).
 */
public class ReportExportAction extends ManagerBaseAction {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(ReportExportAction.class);

    private final transient ReportService reportService = new ReportService();

    private String key;

    @Override
    public String execute() {
        ReportTable table = reportService.report(key);
        HttpServletResponse response = ServletActionContext.getResponse();
        if (table == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return NONE;
        }
        String fileName = key + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        try (PrintWriter out = response.getWriter()) {
            out.write(CsvWriter.toCsv(table));
            out.flush();
        } catch (Exception e) {
            LOG.error("Failed to export report '{}'", key, e);
        }
        audit("REPORT_EXPORT", key);
        return NONE;
    }

    private void audit(String action, String entity) {
        new com.sgx.icms.service.AuditService()
                .success(currentUser(), action, entity, clientIp());
    }

    public void setKey(String key) { this.key = key; }
    public String getKey() { return key; }
}

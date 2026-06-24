package com.sgx.icms.web.action.manager;

import java.util.Collection;
import java.util.Collections;

import com.sgx.icms.service.ReportService;
import com.sgx.icms.web.support.ReportTable;

/** Reports &amp; Analytics screen: renders all report tables. */
public class ReportsAction extends ManagerBaseAction {

    private static final long serialVersionUID = 1L;

    private final transient ReportService reportService = new ReportService();

    private transient Collection<ReportTable> reports = Collections.emptyList();

    @Override
    public String execute() {
        reports = reportService.allReports().values();
        return SUCCESS;
    }

    public Collection<ReportTable> getReports() { return reports; }
}

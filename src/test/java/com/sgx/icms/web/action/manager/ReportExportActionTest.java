package com.sgx.icms.web.action.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.opensymphony.xwork2.Action;
import com.sgx.icms.service.AgentClaimService;
import com.sgx.icms.service.AuditService;
import com.sgx.icms.service.ReportService;
import com.sgx.icms.web.support.ReportTable;

class ReportExportActionTest {

    @Test
    void execute_tableFound_writesCsvAndAudits() throws Exception {
        ReportTable table = new ReportTable("claims-volume", "Claims Volume",
                Collections.singletonList("Status"), Collections.singletonList(Collections.singletonList("SETTLED")));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(pw);

        try (MockedConstruction<ReportService> reportMock = mockConstruction(ReportService.class);
             MockedConstruction<AuditService> auditMock = mockConstruction(AuditService.class);
             MockedConstruction<AgentClaimService> claimsMock = mockConstruction(AgentClaimService.class);
             MockedStatic<ServletActionContext> sac = mockStatic(ServletActionContext.class)) {

            sac.when(ServletActionContext::getResponse).thenReturn(response);
            ReportExportAction action = new ReportExportAction();
            when(reportMock.constructed().get(0).report("claims-volume")).thenReturn(table);

            action.setKey("claims-volume");

            assertEquals(Action.NONE, action.execute());
            verify(response).setHeader("Content-Disposition", "attachment; filename=\"claims-volume.csv\"");
            assertTrue(sw.toString().contains("SETTLED"));
            verify(auditMock.constructed().get(0)).success(null, "REPORT_EXPORT", "claims-volume", null);
        }
    }

    @Test
    void execute_tableNotFound_returns404() {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        try (MockedConstruction<ReportService> reportMock = mockConstruction(ReportService.class);
             MockedConstruction<AgentClaimService> claimsMock = mockConstruction(AgentClaimService.class);
             MockedStatic<ServletActionContext> sac = mockStatic(ServletActionContext.class)) {

            sac.when(ServletActionContext::getResponse).thenReturn(response);
            ReportExportAction action = new ReportExportAction();
            when(reportMock.constructed().get(0).report("unknown")).thenReturn(null);

            action.setKey("unknown");

            assertEquals(Action.NONE, action.execute());
            verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}

package com.sgx.icms.web.action.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.opensymphony.xwork2.Action;
import com.sgx.icms.domain.AuditLog;
import com.sgx.icms.service.AdminService;
import com.sgx.icms.service.AuditService;

class AuditExportActionTest {

    @Test
    void execute_writesCsvAndAudits() throws Exception {
        AuditLog entry = new AuditLog();
        entry.setUsername("bob");
        entry.setAction("LOGIN");
        entry.setResult(AuditLog.RESULT_SUCCESS);
        List<AuditLog> entries = Collections.singletonList(entry);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(pw);

        try (MockedConstruction<AdminService> adminMock = mockConstruction(AdminService.class);
             MockedConstruction<AuditService> auditMock = mockConstruction(AuditService.class);
             MockedStatic<ServletActionContext> sac = mockStatic(ServletActionContext.class)) {

            sac.when(ServletActionContext::getResponse).thenReturn(response);

            AuditExportAction action = new AuditExportAction();
            AdminService adminSvc = adminMock.constructed().get(0);
            when(adminSvc.auditLogsForExport(null, null)).thenReturn(entries);

            String result = action.execute();

            assertEquals(Action.NONE, result);
            verify(response).setContentType("text/csv; charset=UTF-8");
            verify(response).setHeader("Content-Disposition", "attachment; filename=\"audit-log.csv\"");

            String csv = sw.toString();
            assertTrue(csv.contains("Timestamp,User,Role,Action,Entity,IP,Result"));
            assertTrue(csv.contains("bob"));

            AuditService auditSvc = auditMock.constructed().get(0);
            verify(auditSvc).success(null, "AUDIT_EXPORT", "audit-log", null);
        }
    }
}

package com.sgx.icms.web.action.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.opensymphony.xwork2.Action;
import com.sgx.icms.service.AgentClaimService;
import com.sgx.icms.service.ReportService;
import com.sgx.icms.web.support.ReportTable;

class ReportsActionTest {

    @Test
    void execute_returnsAllReportTables() {
        Map<String, ReportTable> reports = new LinkedHashMap<>();
        ReportTable table = new ReportTable("k", "t", Collections.emptyList(), Collections.emptyList());
        reports.put("k", table);

        try (MockedConstruction<ReportService> reportMock = mockConstruction(ReportService.class);
             MockedConstruction<AgentClaimService> claimsMock = mockConstruction(AgentClaimService.class)) {

            ReportsAction action = new ReportsAction();
            when(reportMock.constructed().get(0).allReports()).thenReturn(reports);

            assertEquals(Action.SUCCESS, action.execute());
            assertEquals(1, action.getReports().size());
        }
    }
}

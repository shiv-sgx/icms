package com.sgx.icms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import com.sgx.icms.config.DataSourceProvider;
import com.sgx.icms.dao.ReportDao;
import com.sgx.icms.web.support.ReportTable;

class ReportServiceTest {

    private DataSource ds;
    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        ds = mock(DataSource.class);
        conn = mock(Connection.class);
        when(ds.getConnection()).thenReturn(conn);
    }

    @Test
    void allReportsReturnsSixReportsInDisplayOrder() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ReportDao> daoMC = mockConstruction(ReportDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            ReportDao dao = daoMC.constructed().get(0);
            when(dao.claimsByStatus(conn)).thenReturn(Collections.singletonList(new String[] {"SUBMITTED", "3", "10000"}));
            when(dao.claimsByType(conn)).thenReturn(Collections.emptyList());
            when(dao.slaCompliance(conn)).thenReturn(Collections.emptyList());
            when(dao.settlementTat(conn)).thenReturn(Collections.emptyList());
            when(dao.fraudWatch(conn)).thenReturn(Collections.emptyList());
            when(dao.agentPerformance(conn)).thenReturn(Collections.emptyList());

            ReportService svc = new ReportService();
            Map<String, ReportTable> reports = svc.allReports();

            assertEquals(6, reports.size());
            assertEquals(Arrays.asList("claims-volume", "claims-type", "sla-compliance",
                    "settlement-tat", "fraud-detection", "agent-performance"),
                    Arrays.asList(reports.keySet().toArray()));
            ReportTable volume = reports.get("claims-volume");
            assertEquals("Claims Volume by Status", volume.getTitle());
            assertEquals(Arrays.asList("SUBMITTED", "3", "10000"), volume.getRows().get(0));
        }
    }

    @Test
    void reportReturnsSingleTableByKey() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ReportDao> daoMC = mockConstruction(ReportDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            ReportDao dao = daoMC.constructed().get(0);
            when(dao.claimsByStatus(conn)).thenReturn(Collections.emptyList());
            when(dao.claimsByType(conn)).thenReturn(Collections.emptyList());
            when(dao.slaCompliance(conn)).thenReturn(Collections.emptyList());
            when(dao.settlementTat(conn)).thenReturn(Collections.emptyList());
            when(dao.fraudWatch(conn)).thenReturn(Collections.emptyList());
            when(dao.agentPerformance(conn)).thenReturn(Collections.emptyList());

            ReportService svc = new ReportService();
            ReportTable table = svc.report("sla-compliance");

            assertEquals("SLA Compliance", table.getTitle());
            assertTrue(table.isEmpty());
        }
    }

    @Test
    void reportReturnsNullForUnknownKey() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ReportDao> daoMC = mockConstruction(ReportDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            ReportDao dao = daoMC.constructed().get(0);
            when(dao.claimsByStatus(conn)).thenReturn(Collections.emptyList());
            when(dao.claimsByType(conn)).thenReturn(Collections.emptyList());
            when(dao.slaCompliance(conn)).thenReturn(Collections.emptyList());
            when(dao.settlementTat(conn)).thenReturn(Collections.emptyList());
            when(dao.fraudWatch(conn)).thenReturn(Collections.emptyList());
            when(dao.agentPerformance(conn)).thenReturn(Collections.emptyList());

            ReportService svc = new ReportService();
            assertNull(svc.report("does-not-exist"));
        }
    }
}

package com.sgx.icms.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import com.sgx.icms.config.DataSourceProvider;
import com.sgx.icms.dao.ClaimDao;
import com.sgx.icms.dao.JdbcAuditDao;
import com.sgx.icms.dao.SettlementDao;
import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.Settlement;
import com.sgx.icms.web.support.SessionUser;

class ManagerServiceTest {

    private DataSource ds;
    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        ds = mock(DataSource.class);
        conn = mock(Connection.class);
        when(ds.getConnection()).thenReturn(conn);

        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong(1)).thenReturn(7L);
    }

    private SessionUser manager() {
        return new SessionUser(9L, "mgr1", "Manager One", "m@x.com", "MANAGER", "HQ");
    }

    @Test
    void dashboardStatsReturnsFourCounts() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);

            ManagerService svc = new ManagerService();
            long[] stats = svc.dashboardStats();

            assertArrayEquals(new long[] {7L, 7L, 7L, 7L}, stats);
        }
    }

    @Test
    void overrideSettlementThrowsWhenAmountIsNullOrNonPositive() {
        try (MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            ManagerService svc = new ManagerService();
            assertThrows(IllegalArgumentException.class,
                    () -> svc.overrideSettlement(manager(), 5L, null, "why", "1.1.1.1"));
            assertThrows(IllegalArgumentException.class,
                    () -> svc.overrideSettlement(manager(), 5L, BigDecimal.ZERO, "why", "1.1.1.1"));
        }
    }

    @Test
    void overrideSettlementThrowsWhenNoSettlementExists() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(settlementDaoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(null);

            ManagerService svc = new ManagerService();
            assertThrows(IllegalStateException.class,
                    () -> svc.overrideSettlement(manager(), 5L, BigDecimal.valueOf(1000), "why", "1.1.1.1"));
        }
    }

    @Test
    void overrideSettlementUpdatesAmountAndAuditsWithClaimNo() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(settlementDaoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(new Settlement());
            Claim c = new Claim();
            c.setClaimNo("CLM-2026-0014");
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(c);

            ManagerService svc = new ManagerService();
            svc.overrideSettlement(manager(), 5L, BigDecimal.valueOf(2500), "adjusted", "1.1.1.1");

            verify(settlementDaoMC.constructed().get(0))
                    .updateAmount(eq(conn), eq(5L), eq(BigDecimal.valueOf(2500)), eq("adjusted"));
            verify(auditDaoMC.constructed().get(0)).insert(eq(conn), any());
        }
    }

    @Test
    void overrideSettlementAuditsWithFallbackEntityWhenClaimMissing() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(settlementDaoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(new Settlement());
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(null);

            ManagerService svc = new ManagerService();
            svc.overrideSettlement(manager(), 5L, BigDecimal.valueOf(2500), "adjusted", "1.1.1.1");

            verify(auditDaoMC.constructed().get(0)).insert(eq(conn), any());
        }
    }
}

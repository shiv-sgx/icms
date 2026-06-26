package com.sgx.icms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import com.sgx.icms.config.DataSourceProvider;
import com.sgx.icms.dao.ClaimDao;
import com.sgx.icms.dao.CommunicationDao;
import com.sgx.icms.dao.JdbcAuditDao;
import com.sgx.icms.dao.SettlementDao;
import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.ClaimStatus;
import com.sgx.icms.domain.Settlement;
import com.sgx.icms.web.support.SessionUser;

class SettlementServiceTest {

    private DataSource ds;
    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        ds = mock(DataSource.class);
        conn = mock(Connection.class);
        when(ds.getConnection()).thenReturn(conn);
    }

    private SessionUser manager() {
        return new SessionUser(9L, "mgr1", "Manager One", "m@x.com", "MANAGER", "HQ");
    }

    private Claim claim(String status) {
        Claim c = new Claim();
        c.setClaimNo("CLM-2026-0015");
        c.setStatus(status);
        return c;
    }

    @Test
    void forClaimDelegatesToSettlementDao() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            Settlement s = new Settlement();
            when(settlementDaoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(s);

            SettlementService svc = new SettlementService();
            assertEquals(s, svc.forClaim(5L));
        }
    }

    @Test
    void authorizeThrowsWhenAmountInvalid() {
        try (MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            SettlementService svc = new SettlementService();
            assertThrows(IllegalArgumentException.class,
                    () -> svc.authorize(manager(), 5L, null, "BANK", "h", "b", "a", "i", "j", "1.1.1.1"));
            assertThrows(IllegalArgumentException.class,
                    () -> svc.authorize(manager(), 5L, BigDecimal.ZERO, "BANK", "h", "b", "a", "i", "j", "1.1.1.1"));
        }
    }

    @Test
    void authorizeThrowsWhenClaimNotFound() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(null);

            SettlementService svc = new SettlementService();
            assertThrows(IllegalStateException.class,
                    () -> svc.authorize(manager(), 5L, BigDecimal.valueOf(1000), "BANK", "h", "b", "a", "i", "j", "1.1.1.1"));
        }
    }

    @Test
    void authorizeThrowsWhenClaimNotApprovedAndNoExistingSettlement() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L)))
                    .thenReturn(claim(ClaimStatus.UNDER_REVIEW));
            when(settlementDaoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(null);

            SettlementService svc = new SettlementService();
            assertThrows(IllegalStateException.class,
                    () -> svc.authorize(manager(), 5L, BigDecimal.valueOf(1000), "BANK", "h", "b", "a", "i", "j", "1.1.1.1"));
        }
    }

    @Test
    void authorizeCreatesNewSettlementMovesClaimAndNotifiesAndAudits() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L)))
                    .thenReturn(claim(ClaimStatus.APPROVED));
            when(settlementDaoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(null);

            SettlementService svc = new SettlementService();
            svc.authorize(manager(), 5L, BigDecimal.valueOf(5000), "BANK", "Holder", "Bank", "Acct", "IFSC0001",
                    "ok", "1.1.1.1");

            org.mockito.ArgumentCaptor<Settlement> captor = org.mockito.ArgumentCaptor.forClass(Settlement.class);
            verify(settlementDaoMC.constructed().get(0)).insert(eq(conn), captor.capture());
            assertEquals(Settlement.AUTHORIZED, captor.getValue().getStatus());
            assertEquals(BigDecimal.valueOf(5000), captor.getValue().getFinalAmount());
            verify(claimDaoMC.constructed().get(0))
                    .updateStatus(eq(conn), eq(5L), eq(ClaimStatus.SETTLEMENT_PROCESSING));
            verify(commDaoMC.constructed().get(0)).insert(eq(conn), any());
            verify(auditDaoMC.constructed().get(0)).insert(eq(conn), any());
        }
    }

    @Test
    void authorizeUpdatesAmountWhenSettlementAlreadyExists() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L)))
                    .thenReturn(claim(ClaimStatus.SETTLEMENT_PROCESSING));
            when(settlementDaoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(new Settlement());

            SettlementService svc = new SettlementService();
            svc.authorize(manager(), 5L, BigDecimal.valueOf(6000), "BANK", "Holder", "Bank", "Acct", "IFSC0001",
                    "revised", "1.1.1.1");

            verify(settlementDaoMC.constructed().get(0), never()).insert(any(), any());
            verify(settlementDaoMC.constructed().get(0))
                    .updateAmount(eq(conn), eq(5L), eq(BigDecimal.valueOf(6000)), eq("revised"));
            verify(claimDaoMC.constructed().get(0), never()).updateStatus(any(), any(Long.class), any());
        }
    }

    @Test
    void advanceThrowsWhenClaimOrSettlementMissing() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(null);

            SettlementService svc = new SettlementService();
            assertThrows(IllegalStateException.class, () -> svc.advance(manager(), 5L, "1.1.1.1"));
        }
    }

    @Test
    void advanceReturnsCurrentStatusWhenAlreadyAtEndOfTracker() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(claim(ClaimStatus.CLOSED));
            Settlement s = new Settlement();
            s.setStatus(Settlement.CLOSED);
            when(settlementDaoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(s);

            SettlementService svc = new SettlementService();
            String result = svc.advance(manager(), 5L, "1.1.1.1");

            assertEquals(Settlement.CLOSED, result);
            verify(settlementDaoMC.constructed().get(0), never()).advance(any(), any(Long.class), any());
        }
    }

    @Test
    void advanceMovesToPaymentConfirmedSettlesClaimAndNotifies() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L)))
                    .thenReturn(claim(ClaimStatus.SETTLEMENT_PROCESSING));
            Settlement s = new Settlement();
            s.setStatus(Settlement.BANK_PROCESSING);
            s.setFinalAmount(BigDecimal.valueOf(8000));
            when(settlementDaoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(s);

            SettlementService svc = new SettlementService();
            String result = svc.advance(manager(), 5L, "1.1.1.1");

            assertEquals(Settlement.PAYMENT_CONFIRMED, result);
            verify(settlementDaoMC.constructed().get(0)).advance(eq(conn), eq(5L), eq(Settlement.PAYMENT_CONFIRMED));
            verify(claimDaoMC.constructed().get(0)).updateStatus(eq(conn), eq(5L), eq(ClaimStatus.SETTLED));
            verify(commDaoMC.constructed().get(0)).insert(eq(conn), any());
        }
    }

    @Test
    void advanceMovesToClosedAndUpdatesClaimWithoutNotification() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(claim(ClaimStatus.SETTLED));
            Settlement s = new Settlement();
            s.setStatus(Settlement.CLAIMANT_NOTIFIED);
            when(settlementDaoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(s);

            SettlementService svc = new SettlementService();
            String result = svc.advance(manager(), 5L, "1.1.1.1");

            assertEquals(Settlement.CLOSED, result);
            verify(claimDaoMC.constructed().get(0)).updateStatus(eq(conn), eq(5L), eq(ClaimStatus.CLOSED));
            verify(commDaoMC.constructed().get(0), never()).insert(any(), any());
        }
    }

    @Test
    void advanceIntermediateStepOnlyUpdatesTrackerAndAudits() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L)))
                    .thenReturn(claim(ClaimStatus.SETTLEMENT_PROCESSING));
            Settlement s = new Settlement();
            s.setStatus(Settlement.AUTHORIZED);
            when(settlementDaoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(s);

            SettlementService svc = new SettlementService();
            String result = svc.advance(manager(), 5L, "1.1.1.1");

            assertEquals(Settlement.PAYMENT_INITIATED, result);
            verify(settlementDaoMC.constructed().get(0)).advance(eq(conn), eq(5L), eq(Settlement.PAYMENT_INITIATED));
            verify(claimDaoMC.constructed().get(0), never()).updateStatus(any(), any(Long.class), any());
            verify(auditDaoMC.constructed().get(0)).insert(eq(conn), any());
        }
    }
}

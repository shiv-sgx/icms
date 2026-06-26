package com.sgx.icms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import com.sgx.icms.dao.ApprovalDao;
import com.sgx.icms.dao.ClaimDao;
import com.sgx.icms.dao.ConfigDao;
import com.sgx.icms.dao.JdbcAuditDao;
import com.sgx.icms.dao.NotificationDao;
import com.sgx.icms.domain.Approval;
import com.sgx.icms.domain.ApprovalThreshold;
import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.ClaimStatus;
import com.sgx.icms.web.support.SessionUser;

class ApprovalServiceTest {

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

    private Claim claim(long agentId) {
        Claim c = new Claim();
        c.setClaimNo("CLM-2026-0007");
        c.setAgentId(agentId);
        return c;
    }

    @Test
    void decideThrowsOnInvalidDecisionString() {
        ApprovalService svc = new ApprovalService();
        assertThrows(IllegalArgumentException.class,
                () -> svc.decide(manager(), 1L, "NOT_A_DECISION", "remarks", "1.1.1.1"));
    }

    @Test
    void decideThrowsWhenClaimNotFound() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(1L))).thenReturn(null);

            ApprovalService svc = new ApprovalService();
            assertThrows(IllegalStateException.class,
                    () -> svc.decide(manager(), 1L, "APPROVED", "remarks", "1.1.1.1"));
        }
    }

    @Test
    void decideThrowsWhenNoPendingApproval() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(1L))).thenReturn(claim(3L));
            when(approvalDaoMC.constructed().get(0).findNextPending(eq(conn), eq(1L))).thenReturn(null);

            ApprovalService svc = new ApprovalService();
            assertThrows(IllegalStateException.class,
                    () -> svc.decide(manager(), 1L, "APPROVED", "remarks", "1.1.1.1"));
        }
    }

    private Approval pendingApproval() {
        Approval a = new Approval();
        a.setId(11L);
        a.setLevel("L2");
        return a;
    }

    @Test
    void decideApprovedWithNoMorePendingMovesClaimToApproved() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(1L))).thenReturn(claim(3L));
            when(approvalDaoMC.constructed().get(0).findNextPending(eq(conn), eq(1L))).thenReturn(pendingApproval());
            when(approvalDaoMC.constructed().get(0).countPending(eq(conn), eq(1L))).thenReturn(0L);

            ApprovalService svc = new ApprovalService();
            String status = svc.decide(manager(), 1L, "approved", "looks good", "1.1.1.1");

            assertEquals(ClaimStatus.APPROVED, status);
            verify(claimDaoMC.constructed().get(0)).updateStatus(eq(conn), eq(1L), eq(ClaimStatus.APPROVED));
            verify(notifDaoMC.constructed().get(0)).insert(eq(conn), any());
            verify(auditDaoMC.constructed().get(0)).insert(eq(conn), any());
        }
    }

    @Test
    void decideApprovedWithMorePendingKeepsClaimPendingApproval() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(1L))).thenReturn(claim(3L));
            when(approvalDaoMC.constructed().get(0).findNextPending(eq(conn), eq(1L))).thenReturn(pendingApproval());
            when(approvalDaoMC.constructed().get(0).countPending(eq(conn), eq(1L))).thenReturn(1L);

            ApprovalService svc = new ApprovalService();
            String status = svc.decide(manager(), 1L, "APPROVED", "remarks", "1.1.1.1");

            assertEquals(ClaimStatus.PENDING_APPROVAL, status);
        }
    }

    @Test
    void decideRejectedMovesClaimToRejected() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(1L))).thenReturn(claim(3L));
            when(approvalDaoMC.constructed().get(0).findNextPending(eq(conn), eq(1L))).thenReturn(pendingApproval());

            ApprovalService svc = new ApprovalService();
            String status = svc.decide(manager(), 1L, "REJECTED", "no", "1.1.1.1");

            assertEquals(ClaimStatus.REJECTED, status);
        }
    }

    @Test
    void decideReturnedMovesClaimToUnderReview() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(1L))).thenReturn(claim(3L));
            when(approvalDaoMC.constructed().get(0).findNextPending(eq(conn), eq(1L))).thenReturn(pendingApproval());

            ApprovalService svc = new ApprovalService();
            String status = svc.decide(manager(), 1L, "RETURNED", "fix this", "1.1.1.1");

            assertEquals(ClaimStatus.UNDER_REVIEW, status);
        }
    }

    @Test
    void decideOnHoldMovesClaimToOnHold() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(1L))).thenReturn(claim(3L));
            when(approvalDaoMC.constructed().get(0).findNextPending(eq(conn), eq(1L))).thenReturn(pendingApproval());

            ApprovalService svc = new ApprovalService();
            String status = svc.decide(manager(), 1L, "ON_HOLD", "wait", "1.1.1.1");

            assertEquals(ClaimStatus.ON_HOLD, status);
        }
    }

    @Test
    void decideDoesNotNotifyWhenClaimHasNoAgent() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            Claim c = new Claim();
            c.setClaimNo("CLM-2026-0008");
            c.setAgentId(null);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(1L))).thenReturn(c);
            when(approvalDaoMC.constructed().get(0).findNextPending(eq(conn), eq(1L))).thenReturn(pendingApproval());
            when(approvalDaoMC.constructed().get(0).countPending(eq(conn), eq(1L))).thenReturn(0L);

            ApprovalService svc = new ApprovalService();
            svc.decide(manager(), 1L, "APPROVED", "ok", "1.1.1.1");

            verify(notifDaoMC.constructed().get(0), never()).insert(any(), any());
        }
    }

    @Test
    void forClaimDelegatesToDao() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);

            ApprovalService svc = new ApprovalService();
            svc.forClaim(5L);

            verify(approvalDaoMC.constructed().get(0)).findByClaim(eq(conn), eq(5L));
        }
    }

    private ApprovalThreshold threshold(String level, BigDecimal min) {
        ApprovalThreshold t = new ApprovalThreshold();
        t.setLevel(level);
        t.setMinAmount(min);
        return t;
    }

    @Test
    void createForwardChainAlwaysRecordsAgentApprovedL1() {
        try (MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class)) {
            when(configDaoMC.constructed().get(0).approvalThresholds(conn)).thenReturn(java.util.Collections.emptyList());

            ApprovalService svc = new ApprovalService();
            SessionUser agent = new SessionUser(2L, "ag1", "Agent One", "a@x.com", "AGENT", "HQ");
            int pending = svc.createForwardChain(conn, 1L, agent, BigDecimal.valueOf(1000));

            assertEquals(0, pending);
            verify(approvalDaoMC.constructed().get(0), times(1)).insert(eq(conn), any());
        }
    }

    @Test
    void createForwardChainAddsL2WhenThresholdRequiresIt() {
        try (MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class)) {
            when(configDaoMC.constructed().get(0).approvalThresholds(conn))
                    .thenReturn(java.util.Arrays.asList(threshold("L2", BigDecimal.valueOf(50000))));

            ApprovalService svc = new ApprovalService();
            SessionUser agent = new SessionUser(2L, "ag1", "Agent One", "a@x.com", "AGENT", "HQ");
            int pending = svc.createForwardChain(conn, 1L, agent, BigDecimal.valueOf(60000));

            assertEquals(1, pending);
            verify(approvalDaoMC.constructed().get(0), times(2)).insert(eq(conn), any());
        }
    }

    @Test
    void createForwardChainAddsL2AndL3WhenAboveBothThresholds() {
        try (MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class)) {
            when(configDaoMC.constructed().get(0).approvalThresholds(conn))
                    .thenReturn(java.util.Arrays.asList(
                            threshold("L2", BigDecimal.valueOf(50000)),
                            threshold("L3", BigDecimal.valueOf(200000))));

            ApprovalService svc = new ApprovalService();
            SessionUser agent = new SessionUser(2L, "ag1", "Agent One", "a@x.com", "AGENT", "HQ");
            int pending = svc.createForwardChain(conn, 1L, agent, BigDecimal.valueOf(250000));

            assertEquals(2, pending);
            verify(approvalDaoMC.constructed().get(0), times(3)).insert(eq(conn), any());
        }
    }

    @Test
    void createForwardChainTreatsNullAmountAsZero() {
        try (MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class)) {
            when(configDaoMC.constructed().get(0).approvalThresholds(conn))
                    .thenReturn(java.util.Arrays.asList(threshold("L2", BigDecimal.valueOf(50000))));

            ApprovalService svc = new ApprovalService();
            SessionUser agent = new SessionUser(2L, "ag1", "Agent One", "a@x.com", "AGENT", "HQ");
            int pending = svc.createForwardChain(conn, 1L, agent, null);

            assertEquals(0, pending);
        }
    }
}

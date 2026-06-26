package com.sgx.icms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import java.util.Collections;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import com.sgx.icms.config.DataSourceProvider;
import com.sgx.icms.dao.ApprovalDao;
import com.sgx.icms.dao.AssessmentDao;
import com.sgx.icms.dao.ClaimDao;
import com.sgx.icms.dao.CommunicationDao;
import com.sgx.icms.dao.DocumentDao;
import com.sgx.icms.dao.JdbcAuditDao;
import com.sgx.icms.dao.NotificationDao;
import com.sgx.icms.dao.SettlementDao;
import com.sgx.icms.domain.Assessment;
import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.ClaimStatus;
import com.sgx.icms.web.support.ClaimBundle;
import com.sgx.icms.web.support.SessionUser;

class AgentClaimServiceTest {

    private DataSource ds;
    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        ds = mock(DataSource.class);
        conn = mock(Connection.class);
        when(ds.getConnection()).thenReturn(conn);
    }

    private SessionUser agent() {
        return new SessionUser(1L, "agent1", "Agent One", "a@x.com", "AGENT", "HQ");
    }

    private Claim claim(String status) {
        Claim c = new Claim();
        c.setClaimNo("CLM-2026-0009");
        c.setStatus(status);
        return c;
    }

    @Test
    void listDelegatesToClaimDaoWithPagination() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).countFiltered(eq(conn), eq("OPEN"), eq("MOTOR"), eq("x")))
                    .thenReturn(1L);
            when(claimDaoMC.constructed().get(0)
                    .findFiltered(eq(conn), eq("OPEN"), eq("MOTOR"), eq("x"), eq(20), eq(0)))
                    .thenReturn(Collections.emptyList());

            AgentClaimService svc = new AgentClaimService();
            svc.list("OPEN", "MOTOR", "x", 1, 20);

            verify(claimDaoMC.constructed().get(0)).findFiltered(eq(conn), eq("OPEN"), eq("MOTOR"), eq("x"), eq(20), eq(0));
        }
    }

    @Test
    void statusCountsDelegatesToClaimDao() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);

            AgentClaimService svc = new AgentClaimService();
            svc.statusCounts();

            verify(claimDaoMC.constructed().get(0)).countByStatus(eq(conn));
        }
    }

    @Test
    void worklistDelegatesToClaimDao() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);

            AgentClaimService svc = new AgentClaimService();
            svc.worklist(10);

            verify(claimDaoMC.constructed().get(0)).worklist(eq(conn), eq(10));
        }
    }

    @Test
    void bundleReturnsNullWhenClaimNotFound() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(null);

            AgentClaimService svc = new AgentClaimService();
            assertNull(svc.bundle(5L));
        }
    }

    @Test
    void bundleAggregatesAllSubResourcesAndTimeline() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            Claim c = claim(ClaimStatus.UNDER_ASSESSMENT);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(c);
            when(docDaoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(Collections.emptyList());
            when(commDaoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(Collections.emptyList());
            when(approvalDaoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(Collections.emptyList());
            when(settlementDaoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(null);
            Assessment a = new Assessment();
            a.setId(99L);
            when(assessDaoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(a);
            when(assessDaoMC.constructed().get(0).findComponents(eq(conn), eq(99L))).thenReturn(Collections.emptyList());

            AgentClaimService svc = new AgentClaimService();
            ClaimBundle bundle = svc.bundle(5L);

            assertEquals(c, bundle.getClaim());
            assertEquals(a, bundle.getAssessment());
            assertEquals(Collections.emptyList(), bundle.getComponents());
            assertEquals("Under Review", bundle.getTimeline().get(1).getLabel());
        }
    }

    @Test
    void acknowledgeThrowsWhenClaimNotFound() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(null);

            AgentClaimService svc = new AgentClaimService();
            assertThrows(IllegalStateException.class, () -> svc.acknowledge(agent(), 5L, "1.1.1.1"));
        }
    }

    @Test
    void acknowledgeThrowsWhenClaimNotSubmitted() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(claim(ClaimStatus.UNDER_REVIEW));

            AgentClaimService svc = new AgentClaimService();
            assertThrows(IllegalStateException.class, () -> svc.acknowledge(agent(), 5L, "1.1.1.1"));
        }
    }

    @Test
    void acknowledgeMovesSubmittedClaimToUnderReviewAndAudits() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(claim(ClaimStatus.SUBMITTED));

            AgentClaimService svc = new AgentClaimService();
            svc.acknowledge(agent(), 5L, "1.1.1.1");

            verify(claimDaoMC.constructed().get(0)).acknowledge(eq(conn), eq(5L), eq(1L), eq(ClaimStatus.UNDER_REVIEW));
            verify(auditDaoMC.constructed().get(0)).insert(eq(conn), any());
        }
    }

    @Test
    void updateNotesThrowsWhenClaimNotFound() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(null);

            AgentClaimService svc = new AgentClaimService();
            assertThrows(IllegalStateException.class, () -> svc.updateNotes(agent(), 5L, "note", "1.1.1.1"));
        }
    }

    @Test
    void updateNotesUpdatesAndAudits() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(claim(ClaimStatus.UNDER_REVIEW));

            AgentClaimService svc = new AgentClaimService();
            svc.updateNotes(agent(), 5L, "internal note", "1.1.1.1");

            verify(claimDaoMC.constructed().get(0)).updateInternalNotes(eq(conn), eq(5L), eq("internal note"));
            verify(auditDaoMC.constructed().get(0)).insert(eq(conn), any());
        }
    }

    @Test
    void forwardForApprovalThrowsWhenClaimNotFound() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(null);

            AgentClaimService svc = new AgentClaimService();
            assertThrows(IllegalStateException.class, () -> svc.forwardForApproval(agent(), 5L, "1.1.1.1"));
        }
    }

    @Test
    void forwardForApprovalThrowsWhenClaimNotInForwardableStatus() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(claim(ClaimStatus.SUBMITTED));

            AgentClaimService svc = new AgentClaimService();
            assertThrows(IllegalStateException.class, () -> svc.forwardForApproval(agent(), 5L, "1.1.1.1"));
        }
    }

    @Test
    void forwardForApprovalThrowsWhenAlreadyAwaitingApproval() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L)))
                    .thenReturn(claim(ClaimStatus.UNDER_REVIEW));
            when(approvalDaoMC.constructed().get(0).countPending(eq(conn), eq(5L))).thenReturn(1L);

            AgentClaimService svc = new AgentClaimService();
            assertThrows(IllegalStateException.class, () -> svc.forwardForApproval(agent(), 5L, "1.1.1.1"));
        }
    }

    @Test
    void forwardForApprovalUsesAssessmentNetPayableWhenAvailable() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class);
             MockedConstruction<ApprovalService> approvalSvcMC = mockConstruction(ApprovalService.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L)))
                    .thenReturn(claim(ClaimStatus.UNDER_ASSESSMENT));
            when(approvalDaoMC.constructed().get(0).countPending(eq(conn), eq(5L))).thenReturn(0L);
            Assessment a = new Assessment();
            a.setNetPayable(BigDecimal.valueOf(75000));
            when(assessDaoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(a);
            when(approvalSvcMC.constructed().get(0)
                    .createForwardChain(eq(conn), eq(5L), any(), eq(BigDecimal.valueOf(75000))))
                    .thenReturn(1);

            AgentClaimService svc = new AgentClaimService();
            String status = svc.forwardForApproval(agent(), 5L, "1.1.1.1");

            assertEquals(ClaimStatus.PENDING_APPROVAL, status);
            verify(claimDaoMC.constructed().get(0)).updateStatus(eq(conn), eq(5L), eq(ClaimStatus.PENDING_APPROVAL));
            verify(notifDaoMC.constructed().get(0)).insert(eq(conn), any());
        }
    }

    @Test
    void forwardForApprovalFallsBackToEstimatedLossWhenNoAssessment() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<CommunicationDao> commDaoMC = mockConstruction(CommunicationDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<ApprovalDao> approvalDaoMC = mockConstruction(ApprovalDao.class);
             MockedConstruction<SettlementDao> settlementDaoMC = mockConstruction(SettlementDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class);
             MockedConstruction<ApprovalService> approvalSvcMC = mockConstruction(ApprovalService.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            Claim c = claim(ClaimStatus.ON_HOLD);
            c.setEstimatedLoss(BigDecimal.valueOf(10000));
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(c);
            when(approvalDaoMC.constructed().get(0).countPending(eq(conn), eq(5L))).thenReturn(0L);
            when(assessDaoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(null);
            when(approvalSvcMC.constructed().get(0)
                    .createForwardChain(eq(conn), eq(5L), any(), eq(BigDecimal.valueOf(10000))))
                    .thenReturn(0);

            AgentClaimService svc = new AgentClaimService();
            String status = svc.forwardForApproval(agent(), 5L, "1.1.1.1");

            assertEquals(ClaimStatus.APPROVED, status);
            verify(claimDaoMC.constructed().get(0)).updateStatus(eq(conn), eq(5L), eq(ClaimStatus.APPROVED));
            verify(notifDaoMC.constructed().get(0), never()).insert(any(), any());
        }
    }
}

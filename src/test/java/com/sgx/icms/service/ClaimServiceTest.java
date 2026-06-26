package com.sgx.icms.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.util.Collections;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import com.sgx.icms.config.DataSourceProvider;
import com.sgx.icms.dao.ClaimDao;
import com.sgx.icms.dao.DocumentDao;
import com.sgx.icms.dao.JdbcAuditDao;
import com.sgx.icms.dao.NotificationDao;
import com.sgx.icms.dao.PolicyDao;
import com.sgx.icms.dao.PolicyholderDao;
import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.ClaimStatus;
import com.sgx.icms.domain.DocumentRequirement;
import com.sgx.icms.domain.Policy;
import com.sgx.icms.domain.Policyholder;
import com.sgx.icms.web.support.SessionUser;
import com.sgx.icms.web.support.TimelineStage;

class ClaimServiceTest {

    private DataSource ds;
    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        ds = mock(DataSource.class);
        conn = mock(Connection.class);
        when(ds.getConnection()).thenReturn(conn);
    }

    private SessionUser customer() {
        return new SessionUser(4L, "cust1", "Customer One", "c@x.com", "CUSTOMER", "HQ");
    }

    private Policyholder policyholder() {
        Policyholder ph = new Policyholder();
        ph.setId(4L);
        ph.setFirstName("Customer");
        ph.setLastName("One");
        return ph;
    }

    @Test
    void resolveCustomerDelegatesToPolicyholderDao() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<PolicyDao> policyDaoMC = mockConstruction(PolicyDao.class);
             MockedConstruction<PolicyholderDao> phDaoMC = mockConstruction(PolicyholderDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            Policyholder ph = policyholder();
            when(phDaoMC.constructed().get(0).findByEmail(eq(conn), eq("c@x.com"))).thenReturn(ph);

            ClaimService svc = new ClaimService();
            assertEquals(ph, svc.resolveCustomer("c@x.com"));
        }
    }

    @Test
    void policiesForCustomerDelegatesToPolicyDao() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<PolicyDao> policyDaoMC = mockConstruction(PolicyDao.class);
             MockedConstruction<PolicyholderDao> phDaoMC = mockConstruction(PolicyholderDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(policyDaoMC.constructed().get(0).findByPolicyholder(eq(conn), eq(4L)))
                    .thenReturn(Collections.emptyList());

            ClaimService svc = new ClaimService();
            assertEquals(Collections.emptyList(), svc.policiesForCustomer(4L));
        }
    }

    @Test
    void listForCustomerPaginatesViaClaimDao() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<PolicyDao> policyDaoMC = mockConstruction(PolicyDao.class);
             MockedConstruction<PolicyholderDao> phDaoMC = mockConstruction(PolicyholderDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).countByPolicyholder(eq(conn), eq(4L))).thenReturn(1L);
            when(claimDaoMC.constructed().get(0).findByPolicyholder(eq(conn), eq(4L), eq(10), eq(0)))
                    .thenReturn(Collections.emptyList());

            ClaimService svc = new ClaimService();
            svc.listForCustomer(4L, 1, 10);

            verify(claimDaoMC.constructed().get(0)).findByPolicyholder(eq(conn), eq(4L), eq(10), eq(0));
        }
    }

    @Test
    void customerCountsReturnsTotalOpenSettledArray() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<PolicyDao> policyDaoMC = mockConstruction(PolicyDao.class);
             MockedConstruction<PolicyholderDao> phDaoMC = mockConstruction(PolicyholderDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).countByPolicyholder(eq(conn), eq(4L))).thenReturn(5L);
            when(claimDaoMC.constructed().get(0).countByPolicyholderInStatuses(eq(conn), eq(4L), anyList()))
                    .thenReturn(3L, 2L);

            ClaimService svc = new ClaimService();
            long[] counts = svc.customerCounts(4L);

            assertArrayEquals(new long[] {5L, 3L, 2L}, counts);
        }
    }

    @Test
    void getOwnedClaimReturnsNullWhenNotOwner() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<PolicyDao> policyDaoMC = mockConstruction(PolicyDao.class);
             MockedConstruction<PolicyholderDao> phDaoMC = mockConstruction(PolicyholderDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            Claim c = new Claim();
            c.setPolicyholderId(99L);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(7L))).thenReturn(c);

            ClaimService svc = new ClaimService();
            assertNull(svc.getOwnedClaim(4L, 7L));
        }
    }

    @Test
    void getOwnedClaimReturnsNullWhenClaimMissing() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<PolicyDao> policyDaoMC = mockConstruction(PolicyDao.class);
             MockedConstruction<PolicyholderDao> phDaoMC = mockConstruction(PolicyholderDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(7L))).thenReturn(null);

            ClaimService svc = new ClaimService();
            assertNull(svc.getOwnedClaim(4L, 7L));
        }
    }

    @Test
    void getOwnedClaimReturnsClaimWhenOwned() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<PolicyDao> policyDaoMC = mockConstruction(PolicyDao.class);
             MockedConstruction<PolicyholderDao> phDaoMC = mockConstruction(PolicyholderDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            Claim c = new Claim();
            c.setPolicyholderId(4L);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(7L))).thenReturn(c);

            ClaimService svc = new ClaimService();
            assertEquals(c, svc.getOwnedClaim(4L, 7L));
        }
    }

    private Policy policy(long phId) {
        Policy p = new Policy();
        p.setId(2L);
        p.setPolicyholderId(phId);
        p.setType("MOTOR");
        return p;
    }

    @Test
    void createClaimThrowsWhenPolicyNotOwnedByCustomer() {
        try (MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<PolicyDao> policyDaoMC = mockConstruction(PolicyDao.class);
             MockedConstruction<PolicyholderDao> phDaoMC = mockConstruction(PolicyholderDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class)) {
                dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
                when(policyDaoMC.constructed().get(0).findById(eq(conn), eq(2L))).thenReturn(policy(99L));

                ClaimService svc = new ClaimService();
                Claim draft = new Claim();
                draft.setPolicyId(2L);
                assertThrows(IllegalArgumentException.class,
                        () -> svc.createClaim(customer(), policyholder(), draft, true, "1.1.1.1"));
            }
        }
    }

    @Test
    void createClaimSubmitsFillsDefaultsNotifiesAndAudits() {
        try (MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<PolicyDao> policyDaoMC = mockConstruction(PolicyDao.class);
             MockedConstruction<PolicyholderDao> phDaoMC = mockConstruction(PolicyholderDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class)) {
                dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
                when(policyDaoMC.constructed().get(0).findById(eq(conn), eq(2L))).thenReturn(policy(4L));
                when(claimDaoMC.constructed().get(0).nextClaimNo(eq(conn), any(Integer.class)))
                        .thenReturn("CLM-2026-0010");
                when(claimDaoMC.constructed().get(0).insert(eq(conn), any(Claim.class))).thenReturn(11L);
                when(docDaoMC.constructed().get(0).findRequirements(eq(conn), eq("MOTOR"), any()))
                        .thenReturn(Collections.emptyList());

                ClaimService svc = new ClaimService();
                Claim draft = new Claim();
                draft.setPolicyId(2L);
                long id = svc.createClaim(customer(), policyholder(), draft, true, "1.1.1.1");

                assertEquals(11L, id);
                assertEquals(ClaimStatus.SUBMITTED, draft.getStatus());
                assertEquals("MOTOR", draft.getClaimType());
                assertEquals("Customer One", draft.getClaimantName());
                assertEquals("LOW", draft.getRiskLevel());
                assertEquals(0, draft.getFraudScore());
                verify(notifDaoMC.constructed().get(0)).insert(eq(conn), any());
                verify(auditDaoMC.constructed().get(0)).insert(eq(conn), any());
            }
        }
    }

    @Test
    void createClaimDraftDoesNotNotifyAgents() {
        try (MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<PolicyDao> policyDaoMC = mockConstruction(PolicyDao.class);
             MockedConstruction<PolicyholderDao> phDaoMC = mockConstruction(PolicyholderDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class)) {
                dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
                when(policyDaoMC.constructed().get(0).findById(eq(conn), eq(2L))).thenReturn(policy(4L));
                when(claimDaoMC.constructed().get(0).nextClaimNo(eq(conn), any(Integer.class)))
                        .thenReturn("CLM-2026-0011");
                when(claimDaoMC.constructed().get(0).insert(eq(conn), any(Claim.class))).thenReturn(12L);
                when(docDaoMC.constructed().get(0).findRequirements(eq(conn), eq("MOTOR"), any()))
                        .thenReturn(Collections.emptyList());

                ClaimService svc = new ClaimService();
                Claim draft = new Claim();
                draft.setPolicyId(2L);
                draft.setClaimantName("Explicit Name");
                svc.createClaim(customer(), policyholder(), draft, false, "1.1.1.1");

                assertEquals(ClaimStatus.DRAFT, draft.getStatus());
                assertEquals("Explicit Name", draft.getClaimantName());
                verify(notifDaoMC.constructed().get(0), never()).insert(any(), any());
            }
        }
    }

    @Test
    void createClaimSeedsRequiredDocuments() {
        try (MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<PolicyDao> policyDaoMC = mockConstruction(PolicyDao.class);
             MockedConstruction<PolicyholderDao> phDaoMC = mockConstruction(PolicyholderDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class)) {
                dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
                when(policyDaoMC.constructed().get(0).findById(eq(conn), eq(2L))).thenReturn(policy(4L));
                when(claimDaoMC.constructed().get(0).nextClaimNo(eq(conn), any(Integer.class)))
                        .thenReturn("CLM-2026-0012");
                when(claimDaoMC.constructed().get(0).insert(eq(conn), any(Claim.class))).thenReturn(13L);
                DocumentRequirement req = new DocumentRequirement();
                req.setDocType("FIR");
                req.setRequired(true);
                when(docDaoMC.constructed().get(0).findRequirements(eq(conn), eq("MOTOR"), any()))
                        .thenReturn(Collections.singletonList(req));

                ClaimService svc = new ClaimService();
                Claim draft = new Claim();
                draft.setPolicyId(2L);
                svc.createClaim(customer(), policyholder(), draft, true, "1.1.1.1");

                verify(docDaoMC.constructed().get(0)).insert(eq(conn), any());
            }
        }
    }

    @Test
    void withdrawReturnsFalseWhenNotOwner() {
        try (MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<PolicyDao> policyDaoMC = mockConstruction(PolicyDao.class);
             MockedConstruction<PolicyholderDao> phDaoMC = mockConstruction(PolicyholderDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class)) {
                dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
                Claim c = new Claim();
                c.setPolicyholderId(99L);
                when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(7L))).thenReturn(c);

                ClaimService svc = new ClaimService();
                assertFalse(svc.withdraw(customer(), 4L, 7L, "1.1.1.1"));
                verify(claimDaoMC.constructed().get(0), never()).updateStatus(any(), any(Long.class), any());
            }
        }
    }

    @Test
    void withdrawThrowsWhenStatusNotWithdrawable() {
        try (MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<PolicyDao> policyDaoMC = mockConstruction(PolicyDao.class);
             MockedConstruction<PolicyholderDao> phDaoMC = mockConstruction(PolicyholderDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class)) {
                dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
                Claim c = new Claim();
                c.setPolicyholderId(4L);
                c.setStatus(ClaimStatus.SETTLED);
                when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(7L))).thenReturn(c);

                ClaimService svc = new ClaimService();
                assertThrows(IllegalStateException.class, () -> svc.withdraw(customer(), 4L, 7L, "1.1.1.1"));
            }
        }
    }

    @Test
    void withdrawUpdatesStatusNotifiesAgentsAndAudits() {
        try (MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<PolicyDao> policyDaoMC = mockConstruction(PolicyDao.class);
             MockedConstruction<PolicyholderDao> phDaoMC = mockConstruction(PolicyholderDao.class);
             MockedConstruction<DocumentDao> docDaoMC = mockConstruction(DocumentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class)) {
                dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
                Claim c = new Claim();
                c.setPolicyholderId(4L);
                c.setClaimNo("CLM-2026-0013");
                c.setStatus(ClaimStatus.SUBMITTED);
                when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(7L))).thenReturn(c);

                ClaimService svc = new ClaimService();
                assertTrue(svc.withdraw(customer(), 4L, 7L, "1.1.1.1"));

                verify(claimDaoMC.constructed().get(0)).updateStatus(eq(conn), eq(7L), eq(ClaimStatus.WITHDRAWN));
                verify(notifDaoMC.constructed().get(0)).insert(eq(conn), any());
                verify(auditDaoMC.constructed().get(0)).insert(eq(conn), any());
            }
        }
    }

    @Test
    void timelineForDraftClaimShowsDraftAsCurrentAndRestPending() {
        ClaimService svc = new ClaimService();
        Claim c = new Claim();
        c.setStatus(ClaimStatus.DRAFT);

        java.util.List<TimelineStage> stages = svc.timeline(c);

        assertEquals("Draft", stages.get(0).getLabel());
        assertEquals(TimelineStage.CURRENT, stages.get(0).getState());
        assertEquals(TimelineStage.PENDING, stages.get(1).getState());
    }

    @Test
    void timelineForUnderReviewMarksFirstStageDoneAndSecondCurrent() {
        ClaimService svc = new ClaimService();
        Claim c = new Claim();
        c.setStatus(ClaimStatus.UNDER_REVIEW);

        java.util.List<TimelineStage> stages = svc.timeline(c);

        assertEquals(TimelineStage.DONE, stages.get(0).getState());
        assertEquals(TimelineStage.CURRENT, stages.get(1).getState());
    }

    @Test
    void timelineForOnHoldTreatsPendingApprovalAsCurrent() {
        ClaimService svc = new ClaimService();
        Claim c = new Claim();
        c.setStatus(ClaimStatus.ON_HOLD);

        java.util.List<TimelineStage> stages = svc.timeline(c);

        int pendingApprovalIdx = ClaimStatus.LIFECYCLE.indexOf(ClaimStatus.PENDING_APPROVAL);
        assertEquals(TimelineStage.CURRENT, stages.get(pendingApprovalIdx).getState());
    }

    @Test
    void timelineForRejectedMarksAllStagesPending() {
        ClaimService svc = new ClaimService();
        Claim c = new Claim();
        c.setStatus(ClaimStatus.REJECTED);

        java.util.List<TimelineStage> stages = svc.timeline(c);

        for (TimelineStage s : stages) {
            assertEquals(TimelineStage.PENDING, s.getState());
        }
    }
}

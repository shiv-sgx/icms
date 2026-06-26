package com.sgx.icms.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import com.sgx.icms.config.DataSourceProvider;
import com.sgx.icms.dao.AssessmentDao;
import com.sgx.icms.dao.ClaimDao;
import com.sgx.icms.dao.JdbcAuditDao;
import com.sgx.icms.dao.NotificationDao;
import com.sgx.icms.domain.Assessment;
import com.sgx.icms.domain.AssessmentComponent;
import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.ClaimStatus;
import com.sgx.icms.web.support.SessionUser;

class SurveyorServiceTest {

    private DataSource ds;
    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        ds = mock(DataSource.class);
        conn = mock(Connection.class);
        when(ds.getConnection()).thenReturn(conn);
    }

    private SessionUser surveyor() {
        return new SessionUser(6L, "sv1", "Surveyor One", "s@x.com", "SURVEYOR", "HQ");
    }

    @Test
    void assignedClaimsPaginatesViaClaimDao() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).countBySurveyor(eq(conn), eq(6L))).thenReturn(1L);
            when(claimDaoMC.constructed().get(0).findBySurveyor(eq(conn), eq(6L), eq(10), eq(0)))
                    .thenReturn(Collections.emptyList());

            SurveyorService svc = new SurveyorService();
            svc.assignedClaims(6L, 1, 10);

            verify(claimDaoMC.constructed().get(0)).findBySurveyor(eq(conn), eq(6L), eq(10), eq(0));
        }
    }

    @Test
    void countsReturnsTotalPendingAssessedArray() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).countBySurveyor(eq(conn), eq(6L))).thenReturn(9L);
            when(claimDaoMC.constructed().get(0).countBySurveyorInStatuses(eq(conn), eq(6L), anyList()))
                    .thenReturn(4L, 5L);

            SurveyorService svc = new SurveyorService();
            long[] counts = svc.counts(6L);

            assertArrayEquals(new long[] {9L, 4L, 5L}, counts);
        }
    }

    @Test
    void getAssignedClaimReturnsNullWhenNotAssignedToSurveyor() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            Claim c = new Claim();
            c.setSurveyorId(99L);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(c);

            SurveyorService svc = new SurveyorService();
            assertNull(svc.getAssignedClaim(6L, 5L));
        }
    }

    @Test
    void getAssignedClaimReturnsNullWhenSurveyorIdIsNull() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            Claim c = new Claim();
            c.setSurveyorId(null);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(c);

            SurveyorService svc = new SurveyorService();
            assertNull(svc.getAssignedClaim(6L, 5L));
        }
    }

    @Test
    void getAssignedClaimReturnsClaimWhenAssigned() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            Claim c = new Claim();
            c.setSurveyorId(6L);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(c);

            SurveyorService svc = new SurveyorService();
            assertEquals(c, svc.getAssignedClaim(6L, 5L));
        }
    }

    @Test
    void latestAssessmentAndComponentsDelegateToAssessmentDao() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            Assessment a = new Assessment();
            when(assessDaoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(a);
            when(assessDaoMC.constructed().get(0).findComponents(eq(conn), eq(20L))).thenReturn(Collections.emptyList());

            SurveyorService svc = new SurveyorService();
            assertEquals(a, svc.latestAssessment(5L));
            assertEquals(Collections.emptyList(), svc.components(20L));
        }
    }

    private AssessmentComponent component(BigDecimal cost) {
        AssessmentComponent c = new AssessmentComponent();
        c.setRepairCost(cost);
        return c;
    }

    @Test
    void submitAssessmentThrowsWhenNoComponentsAndNoGrossAssessed() {
        SurveyorService svc = new SurveyorService();
        Assessment input = new Assessment();
        assertThrows(IllegalArgumentException.class,
                () -> svc.submitAssessment(surveyor(), 5L, input, Collections.emptyList(), "1.1.1.1"));
    }

    @Test
    void submitAssessmentThrowsWhenClaimNotAssignedToSurveyor() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            Claim c = new Claim();
            c.setSurveyorId(99L);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(c);

            SurveyorService svc = new SurveyorService();
            Assessment input = new Assessment();
            List<AssessmentComponent> components = Arrays.asList(component(BigDecimal.valueOf(1000)));
            assertThrows(IllegalStateException.class,
                    () -> svc.submitAssessment(surveyor(), 5L, input, components, "1.1.1.1"));
        }
    }

    @Test
    void submitAssessmentThrowsWhenClaimIsTerminal() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            Claim c = new Claim();
            c.setSurveyorId(6L);
            c.setStatus(ClaimStatus.CLOSED);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(c);

            SurveyorService svc = new SurveyorService();
            Assessment input = new Assessment();
            List<AssessmentComponent> components = Arrays.asList(component(BigDecimal.valueOf(1000)));
            assertThrows(IllegalStateException.class,
                    () -> svc.submitAssessment(surveyor(), 5L, input, components, "1.1.1.1"));
        }
    }

    @Test
    void submitAssessmentThrowsWhenAlreadySubmitted() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            Claim c = new Claim();
            c.setSurveyorId(6L);
            c.setStatus(ClaimStatus.SURVEY_SCHEDULED);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(c);
            Assessment existing = new Assessment();
            existing.setStatus(Assessment.STATUS_SUBMITTED);
            when(assessDaoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(existing);

            SurveyorService svc = new SurveyorService();
            Assessment input = new Assessment();
            List<AssessmentComponent> components = Arrays.asList(component(BigDecimal.valueOf(1000)));
            assertThrows(IllegalStateException.class,
                    () -> svc.submitAssessment(surveyor(), 5L, input, components, "1.1.1.1"));
        }
    }

    @Test
    void submitAssessmentComputesNetPayableAndPersistsComponents() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            Claim c = new Claim();
            c.setSurveyorId(6L);
            c.setStatus(ClaimStatus.SURVEY_SCHEDULED);
            c.setClaimNo("CLM-2026-0016");
            c.setAgentId(3L);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(c);
            when(assessDaoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(null);
            when(assessDaoMC.constructed().get(0).insert(eq(conn), any(Assessment.class))).thenReturn(20L);

            SurveyorService svc = new SurveyorService();
            Assessment input = new Assessment();
            input.setPolicyDeductible(BigDecimal.valueOf(500));
            input.setDepreciationPct(BigDecimal.valueOf(10));
            input.setSalvageValue(BigDecimal.valueOf(200));
            List<AssessmentComponent> components = Arrays.asList(
                    component(BigDecimal.valueOf(6000)), component(BigDecimal.valueOf(4000)));

            svc.submitAssessment(surveyor(), 5L, input, components, "1.1.1.1");

            // gross=10000, deprAmt=1000 (10%), net = 10000 - 500 - 1000 - 200 = 8300
            assertEquals(BigDecimal.valueOf(10000), input.getGrossAssessed());
            assertEquals(new BigDecimal("1000.00"), input.getDepreciationAmt());
            assertEquals(new BigDecimal("8300.00"), input.getNetPayable());
            assertEquals(Assessment.STATUS_SUBMITTED, input.getStatus());
            assertEquals(6L, input.getSurveyorId());

            verify(assessDaoMC.constructed().get(0), times(2)).insertComponent(eq(conn), any());
            verify(claimDaoMC.constructed().get(0)).updateStatus(eq(conn), eq(5L), eq(ClaimStatus.UNDER_ASSESSMENT));
            verify(notifDaoMC.constructed().get(0)).insert(eq(conn), any());
            verify(auditDaoMC.constructed().get(0)).insert(eq(conn), any());
        }
    }

    @Test
    void submitAssessmentSkipsNotificationWhenClaimHasNoAgent() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<AssessmentDao> assessDaoMC = mockConstruction(AssessmentDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            Claim c = new Claim();
            c.setSurveyorId(6L);
            c.setStatus(ClaimStatus.SURVEY_SCHEDULED);
            c.setClaimNo("CLM-2026-0017");
            c.setAgentId(null);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(c);
            when(assessDaoMC.constructed().get(0).findByClaim(eq(conn), eq(5L))).thenReturn(null);
            when(assessDaoMC.constructed().get(0).insert(eq(conn), any(Assessment.class))).thenReturn(21L);

            SurveyorService svc = new SurveyorService();
            Assessment input = new Assessment();
            input.setGrossAssessed(BigDecimal.valueOf(5000));

            svc.submitAssessment(surveyor(), 5L, input, null, "1.1.1.1");

            verify(notifDaoMC.constructed().get(0), never()).insert(any(), any());
        }
    }
}

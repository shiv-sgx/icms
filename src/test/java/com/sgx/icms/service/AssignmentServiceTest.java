package com.sgx.icms.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
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
import com.sgx.icms.dao.JdbcAuditDao;
import com.sgx.icms.dao.JdbcUserDao;
import com.sgx.icms.dao.NotificationDao;
import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.ClaimStatus;
import com.sgx.icms.domain.User;
import com.sgx.icms.web.support.SessionUser;

class AssignmentServiceTest {

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

    @Test
    void availableSurveyorsDelegatesToUserDao() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(userDaoMC.constructed().get(0).findActiveByRole(eq(conn), eq("SURVEYOR")))
                    .thenReturn(Collections.emptyList());

            AssignmentService svc = new AssignmentService();
            svc.availableSurveyors();

            verify(userDaoMC.constructed().get(0)).findActiveByRole(eq(conn), eq("SURVEYOR"));
        }
    }

    @Test
    void assignSurveyorThrowsWhenClaimNotFound() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(null);

            AssignmentService svc = new AssignmentService();
            assertThrows(IllegalStateException.class, () -> svc.assignSurveyor(agent(), 5L, 2L, "1.1.1.1"));
        }
    }

    @Test
    void assignSurveyorThrowsWhenClaimIsTerminal() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            Claim claim = new Claim();
            claim.setStatus(ClaimStatus.CLOSED);
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(claim);

            AssignmentService svc = new AssignmentService();
            assertThrows(IllegalStateException.class, () -> svc.assignSurveyor(agent(), 5L, 2L, "1.1.1.1"));
        }
    }

    @Test
    void assignSurveyorThrowsWhenChosenUserIsNotASurveyor() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            Claim claim = new Claim();
            claim.setStatus(ClaimStatus.UNDER_REVIEW);
            claim.setClaimNo("CLM-2026-0001");
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(claim);
            User notASurveyor = new User();
            notASurveyor.setRoleName("AGENT");
            when(userDaoMC.constructed().get(0).findById(eq(conn), eq(2L))).thenReturn(notASurveyor);

            AssignmentService svc = new AssignmentService();
            assertThrows(IllegalArgumentException.class, () -> svc.assignSurveyor(agent(), 5L, 2L, "1.1.1.1"));
        }
    }

    @Test
    void assignSurveyorSucceedsNotifiesAndAudits() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<ClaimDao> claimDaoMC = mockConstruction(ClaimDao.class);
             MockedConstruction<NotificationDao> notifDaoMC = mockConstruction(NotificationDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            Claim claim = new Claim();
            claim.setStatus(ClaimStatus.UNDER_REVIEW);
            claim.setClaimNo("CLM-2026-0001");
            when(claimDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(claim);
            User surveyor = new User();
            surveyor.setRoleName("SURVEYOR");
            surveyor.setUsername("sv1");
            surveyor.setFullName("Surveyor One");
            when(userDaoMC.constructed().get(0).findById(eq(conn), eq(2L))).thenReturn(surveyor);

            AssignmentService svc = new AssignmentService();
            svc.assignSurveyor(agent(), 5L, 2L, "1.1.1.1");

            verify(claimDaoMC.constructed().get(0))
                    .assignSurveyor(eq(conn), eq(5L), eq(2L), eq(1L), eq(ClaimStatus.SURVEY_SCHEDULED));
            verify(notifDaoMC.constructed().get(0)).insert(eq(conn), any());
            verify(auditDaoMC.constructed().get(0)).insert(eq(conn), any());
        }
    }
}

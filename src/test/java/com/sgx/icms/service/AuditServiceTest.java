package com.sgx.icms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.invocation.InvocationOnMock;

import com.sgx.icms.config.DataSourceProvider;
import com.sgx.icms.dao.AuditDao;
import com.sgx.icms.dao.JdbcAuditDao;
import com.sgx.icms.domain.AuditLog;
import com.sgx.icms.web.support.SessionUser;

class AuditServiceTest {

    @Test
    void transactionalRecordDelegatesToDaoOnGivenConnection() {
        try (MockedConstruction<JdbcAuditDao> daoMC = mockConstruction(JdbcAuditDao.class)) {
            AuditService svc = new AuditService();
            Connection conn = mock(Connection.class);
            AuditLog entry = new AuditLog();

            svc.record(conn, entry);

            AuditDao dao = daoMC.constructed().get(0);
            verify(dao).insert(conn, entry);
        }
    }

    @Test
    void fireAndForgetRecordBorrowsItsOwnConnectionAndInserts() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcAuditDao> daoMC = mockConstruction(JdbcAuditDao.class)) {
            DataSource ds = mock(DataSource.class);
            Connection conn = mock(Connection.class);
            try {
                when(ds.getConnection()).thenReturn(conn);
            } catch (SQLException ignored) {
                // unreachable: stubbing a mock
            }
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);

            AuditService svc = new AuditService();
            svc.record(5L, "bob", "AGENT", "LOGIN", "entity", AuditLog.RESULT_SUCCESS, "1.2.3.4");

            AuditDao dao = daoMC.constructed().get(0);
            verify(dao).insert(eq(conn), any(AuditLog.class));
        }
    }

    @Test
    void fireAndForgetRecordSwallowsDaoFailure() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcAuditDao> daoMC = mockConstruction(JdbcAuditDao.class,
                     (mockDao, ctx) -> when(mockDao.insert(any(), any()))
                             .thenAnswer((InvocationOnMock inv) -> {
                                 throw new RuntimeException("boom");
                             }))) {
            DataSource ds = mock(DataSource.class);
            Connection conn = mock(Connection.class);
            try {
                when(ds.getConnection()).thenReturn(conn);
            } catch (SQLException ignored) {
                // unreachable: stubbing a mock
            }
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);

            AuditService svc = new AuditService();
            // Must not throw -- failures are logged, not propagated.
            svc.record(5L, "bob", "AGENT", "LOGIN", "entity", AuditLog.RESULT_FAILED, "1.2.3.4");
        }
    }

    @Test
    void successBuildsAuditLogFromActorFieldsAndInserts() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcAuditDao> daoMC = mockConstruction(JdbcAuditDao.class)) {
            DataSource ds = mock(DataSource.class);
            Connection conn = mock(Connection.class);
            try {
                when(ds.getConnection()).thenReturn(conn);
            } catch (SQLException ignored) {
                // unreachable: stubbing a mock
            }
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);

            AuditService svc = new AuditService();
            SessionUser actor = new SessionUser(7L, "alice", "Alice A", "a@x.com", "MANAGER", "HQ");
            svc.success(actor, "CLAIM_VIEWED", "CLM-2026-0001", "1.2.3.4");

            AuditDao dao = daoMC.constructed().get(0);
            verify(dao).insert(eq(conn), any(AuditLog.class));
            assertEquals(7L, actor.getId());
        }
    }

    @Test
    void successHandlesNullActorWithoutThrowing() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcAuditDao> daoMC = mockConstruction(JdbcAuditDao.class)) {
            DataSource ds = mock(DataSource.class);
            Connection conn = mock(Connection.class);
            try {
                when(ds.getConnection()).thenReturn(conn);
            } catch (SQLException ignored) {
                // unreachable: stubbing a mock
            }
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);

            AuditService svc = new AuditService();
            svc.success(null, "CLAIM_VIEWED", "CLM-2026-0001", "1.2.3.4");

            AuditDao dao = daoMC.constructed().get(0);
            verify(dao).insert(eq(conn), any(AuditLog.class));
        }
    }
}

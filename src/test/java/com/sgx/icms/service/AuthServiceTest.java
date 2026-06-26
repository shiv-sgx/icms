package com.sgx.icms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import com.sgx.icms.config.DataSourceProvider;
import com.sgx.icms.dao.JdbcAuditDao;
import com.sgx.icms.dao.JdbcUserDao;
import com.sgx.icms.dao.UserDao;
import com.sgx.icms.domain.User;

class AuthServiceTest {

    private DataSource mockDs;
    private Connection mockConn;

    @BeforeEach
    void setUp() throws Exception {
        mockDs = mock(DataSource.class);
        mockConn = mock(Connection.class);
        when(mockDs.getConnection()).thenReturn(mockConn);
    }

    private User activeUser(String hash) {
        User u = new User();
        u.setId(1L);
        u.setUsername("bob");
        u.setFullName("Bob Builder");
        u.setRoleName("AGENT");
        u.setStatus("ACTIVE");
        u.setPasswordHash(hash);
        return u;
    }

    @Test
    void authenticateReturnsNullWhenUsernameBlank() {
        AuthService svc = new AuthService();
        assertNull(svc.authenticate("", "pw", "1.2.3.4"));
        assertNull(svc.authenticate(null, "pw", "1.2.3.4"));
        assertNull(svc.authenticate("bob", "", "1.2.3.4"));
        assertNull(svc.authenticate("bob", null, "1.2.3.4"));
    }

    @Test
    void authenticateReturnsNullWhenUserNotFound() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<PasswordService> pwMC = mockConstruction(PasswordService.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(mockDs);
            UserDao userDao = userDaoMC.constructed().get(0);
            when(userDao.findByUsername(any(), eq("nobody"))).thenReturn(null);

            AuthService svc = new AuthService();
            User result = svc.authenticate("nobody", "pw", "9.9.9.9");

            assertNull(result);
            JdbcAuditDao auditDao = auditDaoMC.constructed().get(0);
            verify(auditDao).insert(any(), any());
        }
    }

    @Test
    void authenticateReturnsNullWhenAccountNotActive() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<PasswordService> pwMC = mockConstruction(PasswordService.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(mockDs);
            User inactive = activeUser("hash");
            inactive.setStatus("INACTIVE");
            UserDao userDao = userDaoMC.constructed().get(0);
            when(userDao.findByUsername(any(), eq("bob"))).thenReturn(inactive);

            AuthService svc = new AuthService();
            assertNull(svc.authenticate("bob", "pw", "9.9.9.9"));
        }
    }

    @Test
    void authenticateReturnsNullWhenPasswordWrong() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<PasswordService> pwMC = mockConstruction(PasswordService.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(mockDs);
            User user = activeUser("hash");
            UserDao userDao = userDaoMC.constructed().get(0);
            when(userDao.findByUsername(any(), eq("bob"))).thenReturn(user);
            PasswordService pw = pwMC.constructed().get(0);
            when(pw.matches(eq("wrong"), eq("hash"))).thenReturn(false);

            AuthService svc = new AuthService();
            assertNull(svc.authenticate("bob", "wrong", "9.9.9.9"));
        }
    }

    @Test
    void authenticateSucceedsAndUpdatesLastLoginAndAudits() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<PasswordService> pwMC = mockConstruction(PasswordService.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(mockDs);
            User user = activeUser("hash");
            UserDao userDao = userDaoMC.constructed().get(0);
            when(userDao.findByUsername(any(), eq("bob"))).thenReturn(user);
            PasswordService pw = pwMC.constructed().get(0);
            when(pw.matches(eq("correct"), eq("hash"))).thenReturn(true);

            AuthService svc = new AuthService();
            User result = svc.authenticate(" bob ", "correct", "1.1.1.1");

            assertEquals(user, result);
            verify(userDao).updateLastLogin(any(), eq(1L));
            JdbcAuditDao auditDao = auditDaoMC.constructed().get(0);
            verify(auditDao).insert(any(), any());
        }
    }

    @Test
    void authenticateTrimsUsernameBeforeLookup() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<PasswordService> pwMC = mockConstruction(PasswordService.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(mockDs);
            UserDao userDao = userDaoMC.constructed().get(0);
            when(userDao.findByUsername(any(), eq("bob"))).thenReturn(null);

            AuthService svc = new AuthService();
            svc.authenticate("  bob  ", "pw", "1.1.1.1");

            verify(userDao).findByUsername(any(), eq("bob"));
            verify(userDao, never()).updateLastLogin(any(), anyLong());
        }
    }
}

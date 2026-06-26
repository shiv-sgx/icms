package com.sgx.icms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import com.sgx.icms.config.DataSourceProvider;
import com.sgx.icms.dao.ConfigDao;
import com.sgx.icms.dao.JdbcAuditDao;
import com.sgx.icms.dao.JdbcUserDao;
import com.sgx.icms.dao.RoleDao;
import com.sgx.icms.domain.DocumentRequirement;
import com.sgx.icms.domain.User;
import com.sgx.icms.web.support.AdminStats;
import com.sgx.icms.web.support.SessionUser;

class AdminServiceTest {

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
        when(rs.getLong(1)).thenReturn(3L);
    }

    private SessionUser admin() {
        return new SessionUser(1L, "admin1", "Admin One", "ad@x.com", "ADMIN", "HQ");
    }

    @Test
    void statsAggregatesCountsAndHandlesNullHikari() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<RoleDao> roleDaoMC = mockConstruction(RoleDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            dsp.when(DataSourceProvider::hikari).thenReturn(null);

            AdminService svc = new AdminService();
            AdminStats stats = svc.stats();

            assertEquals(3L, stats.getUsers());
            assertEquals(3L, stats.getClaims());
            assertEquals(3L, stats.getRoles());
            assertEquals(3L, stats.getAuditEvents());
        }
    }

    @Test
    void searchUsersDelegatesToUserDaoWithPagination() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<RoleDao> roleDaoMC = mockConstruction(RoleDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(userDaoMC.constructed().get(0).countSearch(eq(conn), eq("bob"), eq("AGENT"))).thenReturn(1L);
            when(userDaoMC.constructed().get(0).search(eq(conn), eq("bob"), eq("AGENT"), eq(20), eq(20)))
                    .thenReturn(Collections.emptyList());

            AdminService svc = new AdminService();
            svc.searchUsers("bob", "AGENT", 2, 20);

            verify(userDaoMC.constructed().get(0)).search(eq(conn), eq("bob"), eq("AGENT"), eq(20), eq(20));
        }
    }

    @Test
    void createUserThrowsWhenRequiredFieldsMissing() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<RoleDao> roleDaoMC = mockConstruction(RoleDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);

            AdminService svc = new AdminService();
            User draft = new User();
            assertThrows(IllegalArgumentException.class,
                    () -> svc.createUser(admin(), draft, "secret1", "1.1.1.1"));
        }
    }

    @Test
    void createUserThrowsWhenPasswordTooShort() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<RoleDao> roleDaoMC = mockConstruction(RoleDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);

            AdminService svc = new AdminService();
            User draft = new User();
            draft.setUsername("bob");
            draft.setEmail("bob@x.com");
            draft.setFullName("Bob B");
            assertThrows(IllegalArgumentException.class,
                    () -> svc.createUser(admin(), draft, "short", "1.1.1.1"));
        }
    }

    @Test
    void createUserThrowsWhenUsernameOrEmailAlreadyExists() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<RoleDao> roleDaoMC = mockConstruction(RoleDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(userDaoMC.constructed().get(0).existsByUsernameOrEmail(eq(conn), eq("bob"), eq("bob@x.com")))
                    .thenReturn(true);

            AdminService svc = new AdminService();
            User draft = new User();
            draft.setUsername("bob");
            draft.setEmail("bob@x.com");
            draft.setFullName("Bob B");
            assertThrows(IllegalStateException.class,
                    () -> svc.createUser(admin(), draft, "secret1", "1.1.1.1"));
            verify(userDaoMC.constructed().get(0), never()).insert(any(), any());
        }
    }

    @Test
    void createUserHashesPasswordInsertsAndAudits() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<RoleDao> roleDaoMC = mockConstruction(RoleDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(userDaoMC.constructed().get(0).existsByUsernameOrEmail(eq(conn), eq("bob"), eq("bob@x.com")))
                    .thenReturn(false);
            when(userDaoMC.constructed().get(0).insert(eq(conn), any(User.class))).thenReturn(7L);

            AdminService svc = new AdminService();
            User draft = new User();
            draft.setUsername("bob");
            draft.setEmail("bob@x.com");
            draft.setFullName("Bob B");
            svc.createUser(admin(), draft, "secret1", "1.1.1.1");

            verify(userDaoMC.constructed().get(0)).insert(eq(conn), eq(draft));
            verify(auditDaoMC.constructed().get(0)).insert(eq(conn), any());
        }
    }

    @Test
    void updateUserThrowsWhenUserNotFound() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<RoleDao> roleDaoMC = mockConstruction(RoleDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(userDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(null);

            AdminService svc = new AdminService();
            assertThrows(IllegalStateException.class,
                    () -> svc.updateUser(admin(), 5L, "ACTIVE", 2, "1.1.1.1"));
        }
    }

    @Test
    void updateUserUpdatesStatusRoleAndAudits() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<RoleDao> roleDaoMC = mockConstruction(RoleDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            User u = new User();
            u.setUsername("bob");
            when(userDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(u);

            AdminService svc = new AdminService();
            svc.updateUser(admin(), 5L, "SUSPENDED", 3, "1.1.1.1");

            verify(userDaoMC.constructed().get(0)).updateStatusAndRole(eq(conn), eq(5L), eq("SUSPENDED"), eq(3));
            verify(auditDaoMC.constructed().get(0)).insert(eq(conn), any());
        }
    }

    @Test
    void resetPasswordThrowsWhenPasswordTooShort() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<RoleDao> roleDaoMC = mockConstruction(RoleDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);

            AdminService svc = new AdminService();
            assertThrows(IllegalArgumentException.class,
                    () -> svc.resetPassword(admin(), 5L, "abc", "1.1.1.1"));
        }
    }

    @Test
    void resetPasswordThrowsWhenUserNotFound() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<RoleDao> roleDaoMC = mockConstruction(RoleDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(userDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(null);

            AdminService svc = new AdminService();
            assertThrows(IllegalStateException.class,
                    () -> svc.resetPassword(admin(), 5L, "newsecret", "1.1.1.1"));
        }
    }

    @Test
    void resetPasswordUpdatesHashAndAudits() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<RoleDao> roleDaoMC = mockConstruction(RoleDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            User u = new User();
            u.setUsername("bob");
            when(userDaoMC.constructed().get(0).findById(eq(conn), eq(5L))).thenReturn(u);

            AdminService svc = new AdminService();
            svc.resetPassword(admin(), 5L, "newsecret", "1.1.1.1");

            verify(userDaoMC.constructed().get(0)).updatePassword(eq(conn), eq(5L), anyString());
            verify(auditDaoMC.constructed().get(0)).insert(eq(conn), any());
        }
    }

    @Test
    void addDocumentRequirementThrowsWhenFieldsMissing() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<RoleDao> roleDaoMC = mockConstruction(RoleDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);

            AdminService svc = new AdminService();
            DocumentRequirement d = new DocumentRequirement();
            assertThrows(IllegalArgumentException.class, () -> svc.addDocumentRequirement(admin(), d, "1.1.1.1"));
        }
    }

    @Test
    void addDocumentRequirementInsertsAndAudits() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<RoleDao> roleDaoMC = mockConstruction(RoleDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);

            AdminService svc = new AdminService();
            DocumentRequirement d = new DocumentRequirement();
            d.setClaimType("MOTOR");
            d.setDocType("FIR");
            svc.addDocumentRequirement(admin(), d, "1.1.1.1");

            verify(configDaoMC.constructed().get(0)).insertDocumentRequirement(eq(conn), eq(d));
            verify(auditDaoMC.constructed().get(0)).insert(eq(conn), any());
        }
    }

    @Test
    void updateSlaDelegatesToConfigDaoAndAudits() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<RoleDao> roleDaoMC = mockConstruction(RoleDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);

            AdminService svc = new AdminService();
            svc.updateSla(admin(), 2, 48, "1.1.1.1");

            verify(configDaoMC.constructed().get(0)).updateSla(eq(conn), eq(2), eq(48));
            verify(auditDaoMC.constructed().get(0)).insert(eq(conn), any());
        }
    }

    @Test
    void updateThresholdDelegatesToConfigDaoAndAudits() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<RoleDao> roleDaoMC = mockConstruction(RoleDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);

            AdminService svc = new AdminService();
            svc.updateThreshold(admin(), 1, BigDecimal.valueOf(1000), BigDecimal.valueOf(5000), "1.1.1.1");

            verify(configDaoMC.constructed().get(0))
                    .updateThreshold(eq(conn), eq(1), eq(BigDecimal.valueOf(1000)), eq(BigDecimal.valueOf(5000)));
        }
    }

    @Test
    void updateTemplateDelegatesToConfigDaoAndAudits() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<RoleDao> roleDaoMC = mockConstruction(RoleDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);

            AdminService svc = new AdminService();
            svc.updateTemplate(admin(), 4, true, "Hello {name}", "1.1.1.1");

            verify(configDaoMC.constructed().get(0)).updateTemplate(eq(conn), eq(4), eq(true), eq("Hello {name}"));
        }
    }

    @Test
    void deleteDocumentRequirementDelegatesToConfigDaoAndAudits() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<RoleDao> roleDaoMC = mockConstruction(RoleDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);

            AdminService svc = new AdminService();
            svc.deleteDocumentRequirement(admin(), 9, "1.1.1.1");

            verify(configDaoMC.constructed().get(0)).deleteDocumentRequirement(eq(conn), eq(9));
            verify(auditDaoMC.constructed().get(0)).insert(eq(conn), any());
        }
    }

    @Test
    void auditLogsDelegatesToAuditDaoWithPagination() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<RoleDao> roleDaoMC = mockConstruction(RoleDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(auditDaoMC.constructed().get(0).count(eq(conn), eq("LOGIN"), eq("SUCCESS"))).thenReturn(2L);
            when(auditDaoMC.constructed().get(0).find(eq(conn), eq("LOGIN"), eq("SUCCESS"), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            AdminService svc = new AdminService();
            svc.auditLogs("LOGIN", "SUCCESS", 1, 10);

            verify(auditDaoMC.constructed().get(0)).find(eq(conn), eq("LOGIN"), eq("SUCCESS"), eq(10), eq(0));
        }
    }

    @Test
    void auditLogsForExportDelegatesToAuditDaoWithLargeLimit() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<JdbcUserDao> userDaoMC = mockConstruction(JdbcUserDao.class);
             MockedConstruction<RoleDao> roleDaoMC = mockConstruction(RoleDao.class);
             MockedConstruction<ConfigDao> configDaoMC = mockConstruction(ConfigDao.class);
             MockedConstruction<JdbcAuditDao> auditDaoMC = mockConstruction(JdbcAuditDao.class)) {
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            when(auditDaoMC.constructed().get(0).find(eq(conn), eq("LOGIN"), eq("SUCCESS"), eq(5000), eq(0)))
                    .thenReturn(Collections.emptyList());

            AdminService svc = new AdminService();
            svc.auditLogsForExport("LOGIN", "SUCCESS");

            verify(auditDaoMC.constructed().get(0)).find(eq(conn), eq("LOGIN"), eq("SUCCESS"), eq(5000), eq(0));
        }
    }
}

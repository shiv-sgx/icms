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
import java.util.Collections;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import com.sgx.icms.config.DataSourceProvider;
import com.sgx.icms.dao.NotificationDao;
import com.sgx.icms.domain.Notification;

class NotificationServiceTest {

    @Test
    void notifyRoleInsertsRoleBroadcastNotification() {
        try (MockedConstruction<NotificationDao> daoMC = mockConstruction(NotificationDao.class)) {
            NotificationService svc = new NotificationService();
            Connection conn = mock(Connection.class);

            svc.notifyRole(conn, "AGENT", "ACTION", "hello agents");

            NotificationDao dao = daoMC.constructed().get(0);
            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(dao).insert(eq(conn), captor.capture());
            assertEquals("AGENT", captor.getValue().getTargetRole());
            assertEquals("ACTION", captor.getValue().getType());
            assertEquals("hello agents", captor.getValue().getMessage());
        }
    }

    @Test
    void notifyUserInsertsUserTargetedNotification() {
        try (MockedConstruction<NotificationDao> daoMC = mockConstruction(NotificationDao.class)) {
            NotificationService svc = new NotificationService();
            Connection conn = mock(Connection.class);

            svc.notifyUser(conn, 42L, "INFO", "hi there");

            NotificationDao dao = daoMC.constructed().get(0);
            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(dao).insert(eq(conn), captor.capture());
            assertEquals(42L, captor.getValue().getUserId());
            assertEquals("INFO", captor.getValue().getType());
        }
    }

    @Test
    void recentForUserDelegatesToDaoWithOwnConnection() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<NotificationDao> daoMC = mockConstruction(NotificationDao.class)) {
            DataSource ds = mock(DataSource.class);
            Connection conn = mock(Connection.class);
            try {
                when(ds.getConnection()).thenReturn(conn);
            } catch (Exception ignored) {
                // unreachable: stubbing a mock
            }
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            NotificationDao dao = daoMC.constructed().get(0);
            when(dao.findForUser(eq(conn), eq(1L), eq("AGENT"), eq(5))).thenReturn(Collections.emptyList());

            NotificationService svc = new NotificationService();
            assertEquals(Collections.emptyList(), svc.recentForUser(1L, "AGENT", 5));
        }
    }
}

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
import com.sgx.icms.dao.CommunicationDao;
import com.sgx.icms.domain.Communication;
import com.sgx.icms.web.support.SessionUser;

class CommunicationServiceTest {

    @Test
    void forClaimDelegatesToDao() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<CommunicationDao> daoMC = mockConstruction(CommunicationDao.class)) {
            DataSource ds = mock(DataSource.class);
            Connection conn = mock(Connection.class);
            try {
                when(ds.getConnection()).thenReturn(conn);
            } catch (Exception ignored) {
                // unreachable: stubbing a mock
            }
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);
            CommunicationDao dao = daoMC.constructed().get(0);
            when(dao.findByClaim(eq(conn), eq(9L))).thenReturn(Collections.emptyList());

            CommunicationService svc = new CommunicationService();
            assertEquals(Collections.emptyList(), svc.forClaim(9L));
        }
    }

    @Test
    void postMessageInsertsUserAuthoredMessage() {
        try (MockedStatic<DataSourceProvider> dsp = mockStatic(DataSourceProvider.class);
             MockedConstruction<CommunicationDao> daoMC = mockConstruction(CommunicationDao.class)) {
            DataSource ds = mock(DataSource.class);
            Connection conn = mock(Connection.class);
            try {
                when(ds.getConnection()).thenReturn(conn);
            } catch (Exception ignored) {
                // unreachable: stubbing a mock
            }
            dsp.when(DataSourceProvider::dataSource).thenReturn(ds);

            CommunicationService svc = new CommunicationService();
            SessionUser actor = new SessionUser(3L, "alice", "Alice A", "a@x.com", "AGENT", "HQ");
            svc.postMessage(actor, 9L, "Please upload your FIR copy.");

            CommunicationDao dao = daoMC.constructed().get(0);
            ArgumentCaptor<Communication> captor = ArgumentCaptor.forClass(Communication.class);
            verify(dao).insert(eq(conn), captor.capture());
            assertEquals(3L, captor.getValue().getSenderId());
            assertEquals("MESSAGE", captor.getValue().getChannel());
            assertEquals("Please upload your FIR copy.", captor.getValue().getContent());
        }
    }

    @Test
    void systemPostsMessageOnCallerSuppliedConnection() {
        try (MockedConstruction<CommunicationDao> daoMC = mockConstruction(CommunicationDao.class)) {
            CommunicationService svc = new CommunicationService();
            Connection conn = mock(Connection.class);

            svc.system(conn, 9L, "ICMS", "Your claim has been received.");

            CommunicationDao dao = daoMC.constructed().get(0);
            ArgumentCaptor<Communication> captor = ArgumentCaptor.forClass(Communication.class);
            verify(dao).insert(eq(conn), captor.capture());
            assertEquals("ICMS", captor.getValue().getSenderName());
            assertEquals("Your claim has been received.", captor.getValue().getContent());
        }
    }
}

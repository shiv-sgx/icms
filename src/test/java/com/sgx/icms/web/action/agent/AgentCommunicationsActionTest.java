package com.sgx.icms.web.action.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import com.sgx.icms.dao.CommunicationDao;
import com.sgx.icms.db.Db;
import com.sgx.icms.domain.Communication;
import com.sgx.icms.web.action.BaseAction;

class AgentCommunicationsActionTest {

    @Test
    void execute_returnsRecentMessagesViaDbWithConnection() {
        try (MockedConstruction<CommunicationDao> daoMock = mockConstruction(CommunicationDao.class);
             MockedStatic<Db> db = mockStatic(Db.class)) {

            List<Communication> recent = Collections.singletonList(new Communication());
            db.when(() -> Db.withConnection(any())).thenAnswer(inv -> {
                Db.ConnectionFunction<?> fn = inv.getArgument(0);
                return fn.apply(mock(Connection.class));
            });
            when(daoMock.constructed().get(0).findRecent(any(), eq(30))).thenReturn(recent);

            AgentCommunicationsAction action = new AgentCommunicationsAction();

            assertEquals(BaseAction.SUCCESS, action.execute());
            assertEquals(recent, action.getMessages());
        }
    }
}

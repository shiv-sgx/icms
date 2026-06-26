package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sgx.icms.domain.Notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationDaoTest {

    private final NotificationDao dao = new NotificationDao();

    @Test
    void findForUser_bindsUserRoleLimit_andMapsNullUserId() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("type")).thenReturn("BROADCAST");
        when(rs.getLong("user_id")).thenReturn(0L);
        when(rs.wasNull()).thenReturn(true);

        List<Notification> result = dao.findForUser(conn, 1L, "AGENT", 20);

        assertEquals("BROADCAST", result.get(0).getType());
        assertNull(result.get(0).getUserId());
        verify(ps).setObject(1, 1L);
        verify(ps).setObject(2, "AGENT");
        verify(ps).setObject(3, 20);
    }

    @Test
    void insert_defaultsTypeToInfo_andReturnsGeneratedKey() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(ps);
        ResultSet keys = mock(ResultSet.class);
        when(ps.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(true);
        when(keys.getLong(1)).thenReturn(9L);

        Notification n = new Notification();
        n.setTargetRole("MANAGER");
        n.setMessage("New claim filed");
        // type left null -> defaults to "INFO"

        assertEquals(9L, dao.insert(conn, n));
        verify(ps).setObject(3, "INFO");
    }
}

package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sgx.icms.domain.AuditLog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcAuditDaoTest {

    private final JdbcAuditDao dao = new JdbcAuditDao();

    @Test
    void insert_defaultsResultToSuccess() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        AuditLog log = new AuditLog();
        log.setUsername("admin");
        log.setAction("LOGIN");
        // result left null -> defaults to AuditLog.RESULT_SUCCESS

        dao.insert(conn, log);

        verify(ps).setObject(7, AuditLog.RESULT_SUCCESS);
    }

    @Test
    void find_noFilters_omitsWhereClause() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("action")).thenReturn("LOGIN");
        when(rs.getLong("user_id")).thenReturn(1L);
        when(rs.wasNull()).thenReturn(false);

        List<AuditLog> result = dao.find(conn, null, null, 10, 0);

        assertEquals(1, result.size());
        assertEquals("LOGIN", result.get(0).getAction());
        verify(conn).prepareStatement(org.mockito.ArgumentMatchers.argThat(sql -> !sql.contains("WHERE")));
    }

    @Test
    void find_withActionAndResultFilters_buildsAndedWhere() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        dao.find(conn, "LOGIN_FAIL", "FAILED", 10, 0);

        verify(ps).setObject(1, "LOGIN_FAIL");
        verify(ps).setObject(2, "FAILED");
        verify(ps).setObject(3, 10);
        verify(ps).setObject(4, 0);
        verify(conn).prepareStatement(org.mockito.ArgumentMatchers.argThat(
                sql -> sql.contains("WHERE") && sql.contains(" AND ")));
    }

    @Test
    void count_withResultFilterOnly() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong(1)).thenReturn(12L);

        assertEquals(12L, dao.count(conn, null, "SUCCESS"));
        verify(ps).setObject(1, "SUCCESS");
    }
}

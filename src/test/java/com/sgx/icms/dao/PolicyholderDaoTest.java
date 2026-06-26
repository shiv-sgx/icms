package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.jupiter.api.Test;

import com.sgx.icms.domain.Policyholder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PolicyholderDaoTest {

    private final PolicyholderDao dao = new PolicyholderDao();

    @Test
    void findByEmail_mapsRow() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("email")).thenReturn("jane@example.com");

        Policyholder p = dao.findByEmail(conn, "jane@example.com");

        assertEquals("jane@example.com", p.getEmail());
        verify(ps).setObject(1, "jane@example.com");
    }

    @Test
    void findByEmail_returnsNull_whenAbsent() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertNull(dao.findByEmail(conn, "nobody@example.com"));
    }

    @Test
    void findById_mapsRow() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("first_name")).thenReturn("Jane");

        Policyholder p = dao.findById(conn, 1L);

        assertEquals("Jane", p.getFirstName());
        verify(ps).setObject(1, 1L);
    }
}

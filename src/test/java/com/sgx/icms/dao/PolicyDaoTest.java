package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sgx.icms.domain.Policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PolicyDaoTest {

    private final PolicyDao dao = new PolicyDao();

    @Test
    void findById_mapsRow() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("policy_no")).thenReturn("POL-001");

        Policy p = dao.findById(conn, 1L);

        assertEquals("POL-001", p.getPolicyNo());
        verify(ps).setObject(1, 1L);
    }

    @Test
    void findById_returnsNull_whenAbsent() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertNull(dao.findById(conn, 999L));
    }

    @Test
    void findByPolicyholder_bindsId() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString("policy_no")).thenReturn("POL-001", "POL-002");

        List<Policy> result = dao.findByPolicyholder(conn, 5L);

        assertEquals(2, result.size());
        verify(ps).setObject(1, 5L);
    }
}

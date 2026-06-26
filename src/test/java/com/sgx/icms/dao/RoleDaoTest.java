package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sgx.icms.domain.Role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoleDaoTest {

    private final RoleDao dao = new RoleDao();

    @Test
    void findAll_includesUserCount() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("name")).thenReturn("ADMIN");
        when(rs.getLong("user_count")).thenReturn(2L);

        List<Role> result = dao.findAll(conn);

        assertEquals("ADMIN", result.get(0).getName());
        assertEquals(2L, result.get(0).getUserCount());
    }

    @Test
    void findAllSimple_mapsWithoutUserCount() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("name")).thenReturn("CUSTOMER");

        List<Role> result = dao.findAllSimple(conn);

        assertEquals("CUSTOMER", result.get(0).getName());
    }
}

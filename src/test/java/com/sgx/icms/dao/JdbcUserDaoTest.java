package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sgx.icms.domain.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcUserDaoTest {

    private final JdbcUserDao dao = new JdbcUserDao();

    @Test
    void findByUsername_mapsRow() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("username")).thenReturn("admin");
        when(rs.getString("role_name")).thenReturn("ADMIN");

        User u = dao.findByUsername(conn, "admin");

        assertEquals("admin", u.getUsername());
        assertEquals("ADMIN", u.getRoleName());
        verify(ps).setObject(1, "admin");
    }

    @Test
    void findById_returnsNull_whenAbsent() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertNull(dao.findById(conn, 1L));
    }

    @Test
    void updateLastLogin_bindsUserId() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        dao.updateLastLogin(conn, 7L);

        verify(ps).setObject(1, 7L);
        verify(ps).executeUpdate();
    }

    @Test
    void findActiveByRole_bindsRoleName() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        dao.findActiveByRole(conn, "SURVEYOR");

        verify(ps).setObject(1, "SURVEYOR");
    }

    @Test
    void search_noFilters_omitsWhereClause() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        List<User> result = dao.search(conn, null, null, 10, 0);

        assertTrue(result.isEmpty());
        verify(ps).setObject(1, 10);
        verify(ps).setObject(2, 0);
        verify(conn).prepareStatement(org.mockito.ArgumentMatchers.argThat(sql -> !sql.contains("WHERE")));
    }

    @Test
    void search_withQueryAndRole_bindsLikeWildcardsAndRole() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        dao.search(conn, "jane", "AGENT", 10, 0);

        verify(ps).setObject(1, "%jane%");
        verify(ps).setObject(2, "%jane%");
        verify(ps).setObject(3, "%jane%");
        verify(ps).setObject(4, "AGENT");
        verify(ps).setObject(5, 10);
        verify(ps).setObject(6, 0);
    }

    @Test
    void countSearch_byRoleOnly() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong(1)).thenReturn(6L);

        assertEquals(6L, dao.countSearch(conn, null, "MANAGER"));
        verify(ps).setObject(1, "MANAGER");
    }

    @Test
    void insert_defaultsStatusToActive_andReturnsGeneratedKey() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(ps);
        ResultSet keys = mock(ResultSet.class);
        when(ps.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(true);
        when(keys.getLong(1)).thenReturn(20L);

        User u = new User();
        u.setFullName("New User");
        u.setUsername("newuser");
        u.setEmail("new@example.com");
        u.setPasswordHash("hash");
        u.setRoleId(2);
        // status left null -> defaults to ACTIVE

        assertEquals(20L, dao.insert(conn, u));
        verify(ps).setObject(7, "ACTIVE");
    }

    @Test
    void updateStatusAndRole_bindsParams() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        dao.updateStatusAndRole(conn, 1L, "SUSPENDED", 3);

        verify(ps).setObject(1, "SUSPENDED");
        verify(ps).setObject(2, 3);
        verify(ps).setObject(3, 1L);
    }

    @Test
    void updatePassword_bindsHashAndId() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        dao.updatePassword(conn, 1L, "newhash");

        verify(ps).setObject(1, "newhash");
        verify(ps).setObject(2, 1L);
    }

    @Test
    void existsByUsernameOrEmail_true_whenCountPositive() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong(1)).thenReturn(1L);

        assertTrue(dao.existsByUsernameOrEmail(conn, "admin", "admin@x.com"));
        verify(ps).setObject(1, "admin");
        verify(ps).setObject(2, "admin@x.com");
    }

    @Test
    void existsByUsernameOrEmail_false_whenCountZero() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong(1)).thenReturn(0L);

        assertFalse(dao.existsByUsernameOrEmail(conn, "nobody", "nobody@x.com"));
    }
}

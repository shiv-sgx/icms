package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sgx.icms.domain.Approval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApprovalDaoTest {

    private final ApprovalDao dao = new ApprovalDao();

    @Test
    void findByClaim_mapsRows_withNullApprover() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("level")).thenReturn("L1");
        when(rs.getLong("approver_id")).thenReturn(0L);
        when(rs.wasNull()).thenReturn(true);

        List<Approval> result = dao.findByClaim(conn, 10L);

        assertEquals(1, result.size());
        assertEquals("L1", result.get(0).getLevel());
        assertNull(result.get(0).getApproverId());
        verify(ps).setObject(1, 10L);
    }

    @Test
    void findNextPending_returnsNull_whenNoRows() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertNull(dao.findNextPending(conn, 99L));
    }

    @Test
    void insert_usesPendingDefault_andReturnsGeneratedKey() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(ps);
        ResultSet keys = mock(ResultSet.class);
        when(ps.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(true);
        when(keys.getLong(1)).thenReturn(42L);

        Approval a = new Approval();
        a.setClaimId(5L);
        a.setLevel("L1");
        a.setApproverRole("MANAGER");
        // decision left null -> DAO should substitute Approval.PENDING

        long id = dao.insert(conn, a);

        assertEquals(42L, id);
        verify(ps).setObject(5, Approval.PENDING);
    }

    @Test
    void decide_bindsParamsInOrder() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        dao.decide(conn, 7L, 3L, "APPROVED", "looks fine");

        verify(ps).setObject(1, 3L);
        verify(ps).setObject(2, "APPROVED");
        verify(ps).setObject(3, "looks fine");
        verify(ps).setObject(4, 7L);
        verify(ps).executeUpdate();
    }

    @Test
    void countPending_returnsScalar() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong(1)).thenReturn(3L);

        assertEquals(3L, dao.countPending(conn, 10L));
    }
}

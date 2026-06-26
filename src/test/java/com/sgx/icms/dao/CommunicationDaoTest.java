package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sgx.icms.domain.Communication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommunicationDaoTest {

    private final CommunicationDao dao = new CommunicationDao();

    @Test
    void findByClaim_mapsRows_withNullSender() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("content")).thenReturn("Please upload your FIR copy");
        when(rs.getLong("sender_id")).thenReturn(0L);
        when(rs.wasNull()).thenReturn(true);

        List<Communication> result = dao.findByClaim(conn, 1L);

        assertEquals(1, result.size());
        assertEquals("Please upload your FIR copy", result.get(0).getContent());
        assertNull(result.get(0).getSenderId());
    }

    @Test
    void findRecent_includesClaimNo() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("claim_no")).thenReturn("CLM-2026-0001");
        when(rs.getLong("sender_id")).thenReturn(1L);
        when(rs.wasNull()).thenReturn(false);

        List<Communication> result = dao.findRecent(conn, 5);

        assertEquals("CLM-2026-0001", result.get(0).getClaimNo());
        verify(ps).setObject(1, 5);
    }

    @Test
    void insert_defaultsChannel_andReturnsGeneratedKey() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(ps);
        ResultSet keys = mock(ResultSet.class);
        when(ps.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(true);
        when(keys.getLong(1)).thenReturn(3L);

        Communication c = new Communication();
        c.setClaimId(1L);
        c.setSenderName("Agent Smith");
        c.setContent("Hello");
        // channel left null -> defaults to "MESSAGE"

        assertEquals(3L, dao.insert(conn, c));
        verify(ps).setObject(4, "MESSAGE");
    }
}

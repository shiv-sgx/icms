package com.sgx.icms.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.jupiter.api.Test;

import com.sgx.icms.domain.Settlement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SettlementDaoTest {

    private final SettlementDao dao = new SettlementDao();

    @Test
    void findByClaim_mapsRow_withNullApprovedBy() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("status")).thenReturn("AUTHORIZED");
        when(rs.getLong("approved_by")).thenReturn(0L);
        when(rs.wasNull()).thenReturn(true);

        Settlement s = dao.findByClaim(conn, 1L);

        assertEquals("AUTHORIZED", s.getStatus());
        assertNull(s.getApprovedBy());
    }

    @Test
    void insert_defaultsStatusToAuthorized_andReturnsGeneratedKey() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(ps);
        ResultSet keys = mock(ResultSet.class);
        when(ps.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(true);
        when(keys.getLong(1)).thenReturn(6L);

        Settlement s = new Settlement();
        s.setClaimId(1L);
        s.setFinalAmount(new BigDecimal("50000"));
        // status left null -> defaults to Settlement.AUTHORIZED

        assertEquals(6L, dao.insert(conn, s));
        verify(ps).setObject(9, Settlement.AUTHORIZED);
    }

    @Test
    void updateAmount_bindsAmountJustificationClaimId() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        dao.updateAmount(conn, 1L, new BigDecimal("45000"), "Revised after assessment");

        verify(ps).setObject(1, new BigDecimal("45000"));
        verify(ps).setObject(2, "Revised after assessment");
        verify(ps).setObject(3, 1L);
    }

    @Test
    void advance_paymentInitiated_stampsMatchingColumn() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        dao.advance(conn, 1L, Settlement.PAYMENT_INITIATED);

        verify(conn).prepareStatement(org.mockito.ArgumentMatchers.argThat(
                sql -> sql.contains("payment_initiated_at")));
        verify(ps).setObject(1, Settlement.PAYMENT_INITIATED);
        verify(ps).setObject(2, 1L);
    }

    @Test
    void advance_closed_stampsClosedAtColumn() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        dao.advance(conn, 1L, Settlement.CLOSED);

        verify(conn).prepareStatement(org.mockito.ArgumentMatchers.argThat(sql -> sql.contains("closed_at")));
    }

    @Test
    void advance_unknownStatus_skipsTimestampColumn() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        dao.advance(conn, 1L, "REJECTED");

        verify(conn).prepareStatement(org.mockito.ArgumentMatchers.argThat(
                sql -> !sql.contains("_at = NOW()")));
        verify(ps).setObject(1, "REJECTED");
        verify(ps).setObject(2, 1L);
    }
}

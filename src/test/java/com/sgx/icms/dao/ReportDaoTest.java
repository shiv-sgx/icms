package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportDaoTest {

    private final ReportDao dao = new ReportDao();

    private static PreparedStatement stubQuery(Connection conn, ResultSet rs) throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        return ps;
    }

    @Test
    void claimsByStatus_mapsFirstThreeColumns() throws Exception {
        Connection conn = mock(Connection.class);
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString(1)).thenReturn("SETTLED");
        when(rs.getString(2)).thenReturn("5");
        when(rs.getString(3)).thenReturn("125000");
        stubQuery(conn, rs);

        List<String[]> result = dao.claimsByStatus(conn);

        assertEquals(1, result.size());
        assertArrayEquals(new String[] { "SETTLED", "5", "125000" }, result.get(0));
    }

    @Test
    void claimsByType_mapsRows() throws Exception {
        Connection conn = mock(Connection.class);
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString(1)).thenReturn("MOTOR");
        when(rs.getString(2)).thenReturn("10");
        when(rs.getString(3)).thenReturn("500000");
        stubQuery(conn, rs);

        List<String[]> result = dao.claimsByType(conn);

        assertArrayEquals(new String[] { "MOTOR", "10", "500000" }, result.get(0));
    }

    @Test
    void slaCompliance_mapsTwoColumns() throws Exception {
        Connection conn = mock(Connection.class);
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString(1)).thenReturn("Breached");
        when(rs.getString(2)).thenReturn("3");
        stubQuery(conn, rs);

        List<String[]> result = dao.slaCompliance(conn);

        assertArrayEquals(new String[] { "Breached", "3" }, result.get(0));
    }

    @Test
    void settlementTat_mapsUnionRows() throws Exception {
        Connection conn = mock(Connection.class);
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, true, true, false);
        when(rs.getString(1)).thenReturn("Settlements Confirmed", "Avg TAT (days, filed→confirmed)", "Total Settled Amount");
        when(rs.getString(2)).thenReturn("12", "4.5", "600000");
        stubQuery(conn, rs);

        List<String[]> result = dao.settlementTat(conn);

        assertEquals(3, result.size());
    }

    @Test
    void fraudWatch_mapsFiveColumns() throws Exception {
        Connection conn = mock(Connection.class);
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString(1)).thenReturn("CLM-2026-0001");
        when(rs.getString(2)).thenReturn("MOTOR");
        when(rs.getString(3)).thenReturn("HIGH");
        when(rs.getString(4)).thenReturn("80");
        when(rs.getString(5)).thenReturn("UNDER_REVIEW");
        stubQuery(conn, rs);

        List<String[]> result = dao.fraudWatch(conn);

        assertArrayEquals(new String[] { "CLM-2026-0001", "MOTOR", "HIGH", "80", "UNDER_REVIEW" }, result.get(0));
    }

    @Test
    void agentPerformance_mapsFourColumns() throws Exception {
        Connection conn = mock(Connection.class);
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString(1)).thenReturn("Agent Smith");
        when(rs.getString(2)).thenReturn("20");
        when(rs.getString(3)).thenReturn("15");
        when(rs.getString(4)).thenReturn("2");
        stubQuery(conn, rs);

        List<String[]> result = dao.agentPerformance(conn);

        assertArrayEquals(new String[] { "Agent Smith", "20", "15", "2" }, result.get(0));
    }
}

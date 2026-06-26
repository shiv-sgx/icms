package com.sgx.icms.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sgx.icms.domain.Assessment;
import com.sgx.icms.domain.AssessmentComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssessmentDaoTest {

    private final AssessmentDao dao = new AssessmentDao();

    @Test
    void findByClaim_mapsRow_withNullSurveyor() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("recommendation")).thenReturn("APPROVE");
        when(rs.getLong("surveyor_id")).thenReturn(0L);
        when(rs.wasNull()).thenReturn(true);

        Assessment a = dao.findByClaim(conn, 1L);

        assertEquals("APPROVE", a.getRecommendation());
        assertNull(a.getSurveyorId());
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

        assertNull(dao.findById(conn, 5L));
    }

    @Test
    void findComponents_mapsList() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString("component")).thenReturn("BUMPER", "DOOR");
        when(rs.getBoolean("replace_flag")).thenReturn(true, false);

        List<AssessmentComponent> comps = dao.findComponents(conn, 9L);

        assertEquals(2, comps.size());
        assertEquals("BUMPER", comps.get(0).getComponent());
        assertEquals("DOOR", comps.get(1).getComponent());
    }

    @Test
    void insert_returnsGeneratedKey() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(ps);
        ResultSet keys = mock(ResultSet.class);
        when(ps.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(true);
        when(keys.getLong(1)).thenReturn(77L);

        Assessment a = new Assessment();
        a.setClaimId(1L);
        a.setGrossAssessed(new BigDecimal("1000.00"));
        a.setStatus("DRAFT");

        assertEquals(77L, dao.insert(conn, a));
        verify(ps).setObject(1, 1L);
    }

    @Test
    void insertComponent_bindsReplaceFlag() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(ps);
        ResultSet keys = mock(ResultSet.class);
        when(ps.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(true);
        when(keys.getLong(1)).thenReturn(1L);

        AssessmentComponent c = new AssessmentComponent();
        c.setAssessmentId(9L);
        c.setComponent("BUMPER");
        c.setReplaceFlag(true);

        dao.insertComponent(conn, c);

        verify(ps).setObject(5, true);
    }
}

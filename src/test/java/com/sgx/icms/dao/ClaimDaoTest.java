package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sgx.icms.domain.Claim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClaimDaoTest {

    private final ClaimDao dao = new ClaimDao();

    @Test
    void findById_mapsNullableAgentAndSurveyor_inCallOrder() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("claim_no")).thenReturn("CLM-2026-0001");
        when(rs.getString("status")).thenReturn("SUBMITTED");
        when(rs.getLong("agent_id")).thenReturn(0L);
        when(rs.getLong("surveyor_id")).thenReturn(5L);
        // mapper reads agent_id+wasNull, then surveyor_id+wasNull, in that order
        when(rs.wasNull()).thenReturn(true, false);

        Claim c = dao.findById(conn, 1L);

        assertEquals("CLM-2026-0001", c.getClaimNo());
        assertNull(c.getAgentId());
        assertEquals(5L, c.getSurveyorId());
        verify(ps).setObject(1, 1L);
    }

    @Test
    void findByPolicyholder_bindsPagingParams() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        dao.findByPolicyholder(conn, 3L, 10, 20);

        verify(ps).setObject(1, 3L);
        verify(ps).setObject(2, 10);
        verify(ps).setObject(3, 20);
    }

    @Test
    void countByPolicyholder_returnsScalar() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong(1)).thenReturn(4L);

        assertEquals(4L, dao.countByPolicyholder(conn, 1L));
    }

    @Test
    void countByPolicyholderInStatuses_emptyList_shortCircuitsWithoutQuery() throws Exception {
        Connection conn = mock(Connection.class);

        long result = dao.countByPolicyholderInStatuses(conn, 1L, List.of());

        assertEquals(0L, result);
        verifyNoQueryExecuted(conn);
    }

    @Test
    void countByPolicyholderInStatuses_buildsInClausePlaceholders() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong(1)).thenReturn(2L);

        long result = dao.countByPolicyholderInStatuses(conn, 1L, Arrays.asList("SETTLED", "CLOSED"));

        assertEquals(2L, result);
        verify(ps).setObject(1, 1L);
        verify(ps).setObject(2, "SETTLED");
        verify(ps).setObject(3, "CLOSED");
    }

    @Test
    void nextClaimNo_formatsWithYearAndSequence() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong(1)).thenReturn(7L);

        assertEquals("CLM-2026-0007", dao.nextClaimNo(conn, 2026));
    }

    @Test
    void insert_defaultsRiskLevel_andReturnsGeneratedKey() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(ps);
        ResultSet keys = mock(ResultSet.class);
        when(ps.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(true);
        when(keys.getLong(1)).thenReturn(15L);

        Claim c = new Claim();
        c.setClaimNo("CLM-2026-0002");
        c.setPolicyId(1L);
        c.setPolicyholderId(2L);
        c.setClaimantName("Jane Doe");
        c.setClaimType("MOTOR");
        c.setStatus("DRAFT");
        // riskLevel left null -> DAO substitutes "LOW"

        assertEquals(15L, dao.insert(conn, c));
        verify(ps).setObject(21, "LOW");
    }

    @Test
    void updateStatus_bindsStatusAndId() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        dao.updateStatus(conn, 9L, "CLOSED");

        verify(ps).setObject(1, "CLOSED");
        verify(ps).setObject(2, 9L);
    }

    @Test
    void countByStatus_aggregatesIntoMap() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString("status")).thenReturn("SETTLED", "REJECTED");
        when(rs.getLong("n")).thenReturn(5L, 2L);

        Map<String, Long> result = dao.countByStatus(conn);

        assertEquals(5L, result.get("SETTLED"));
        assertEquals(2L, result.get("REJECTED"));
    }

    @Test
    void worklist_filtersToActionableStatuses() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        dao.worklist(conn, 25);

        verify(ps).setObject(1, 25);
    }

    @Test
    void assignSurveyor_bindsParamsInOrder() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        dao.assignSurveyor(conn, 1L, 2L, 3L, "SURVEY_SCHEDULED");

        verify(ps).setObject(1, 2L);
        verify(ps).setObject(2, 3L);
        verify(ps).setObject(3, "SURVEY_SCHEDULED");
        verify(ps).setObject(4, 1L);
    }

    @Test
    void acknowledge_bindsAgentStatusAndId() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        dao.acknowledge(conn, 1L, 4L, "UNDER_REVIEW");

        verify(ps).setObject(1, 4L);
        verify(ps).setObject(2, "UNDER_REVIEW");
        verify(ps).setObject(3, 1L);
    }

    @Test
    void updateInternalNotes_binds() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        dao.updateInternalNotes(conn, 1L, "looks fraudulent");

        verify(ps).setObject(1, "looks fraudulent");
        verify(ps).setObject(2, 1L);
    }

    @Test
    void findFiltered_noFilters_omitsWhereClause() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        dao.findFiltered(conn, null, null, null, 10, 0);

        verify(ps).setObject(1, 10);
        verify(ps).setObject(2, 0);
        verify(conn).prepareStatement(org.mockito.ArgumentMatchers.argThat(sql -> !sql.contains("WHERE")));
    }

    @Test
    void findFiltered_allFilters_buildsAndedWhereClause() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        dao.findFiltered(conn, "SUBMITTED", "MOTOR", "jane", 10, 0);

        verify(ps).setObject(1, "SUBMITTED");
        verify(ps).setObject(2, "MOTOR");
        verify(ps).setObject(3, "%jane%");
        verify(ps).setObject(4, 10);
        verify(ps).setObject(5, 0);
        verify(conn).prepareStatement(org.mockito.ArgumentMatchers.argThat(
                sql -> sql.contains("WHERE") && sql.contains(" AND ")));
    }

    @Test
    void countFiltered_byStatusOnly() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong(1)).thenReturn(9L);

        assertEquals(9L, dao.countFiltered(conn, "APPROVED", null, null));
        verify(ps).setObject(1, "APPROVED");
    }

    private static void verifyNoQueryExecuted(Connection conn) throws Exception {
        org.mockito.Mockito.verifyNoInteractions(conn);
    }
}

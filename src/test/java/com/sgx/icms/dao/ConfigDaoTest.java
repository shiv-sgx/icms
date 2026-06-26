package com.sgx.icms.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sgx.icms.domain.ApprovalThreshold;
import com.sgx.icms.domain.DocumentRequirement;
import com.sgx.icms.domain.NotificationTemplate;
import com.sgx.icms.domain.SlaConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfigDaoTest {

    private final ConfigDao dao = new ConfigDao();

    @Test
    void approvalThresholds_mapsRows() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("level")).thenReturn("L1");
        when(rs.getBigDecimal("min_amount")).thenReturn(new BigDecimal("0"));
        when(rs.getBigDecimal("max_amount")).thenReturn(new BigDecimal("50000"));

        List<ApprovalThreshold> result = dao.approvalThresholds(conn);

        assertEquals(1, result.size());
        assertEquals("L1", result.get(0).getLevel());
    }

    @Test
    void updateThreshold_bindsMinMaxId() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        dao.updateThreshold(conn, 1, new BigDecimal("100"), new BigDecimal("200"));

        verify(ps).setObject(1, new BigDecimal("100"));
        verify(ps).setObject(2, new BigDecimal("200"));
        verify(ps).setObject(3, 1);
    }

    @Test
    void slaConfigs_mapsRows() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("stage")).thenReturn("SURVEY");
        when(rs.getInt("hours")).thenReturn(48);

        List<SlaConfig> result = dao.slaConfigs(conn);

        assertEquals("SURVEY", result.get(0).getStage());
        assertEquals(48, result.get(0).getHours());
    }

    @Test
    void updateSla_bindsHoursAndId() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        dao.updateSla(conn, 2, 72);

        verify(ps).setObject(1, 72);
        verify(ps).setObject(2, 2);
    }

    @Test
    void templates_mapsRows() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("name")).thenReturn("CLAIM_SUBMITTED");
        when(rs.getBoolean("active")).thenReturn(true);

        List<NotificationTemplate> result = dao.templates(conn);

        assertEquals("CLAIM_SUBMITTED", result.get(0).getName());
        assertEquals(true, result.get(0).isActive());
    }

    @Test
    void updateTemplate_bindsActiveBodyId() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        dao.updateTemplate(conn, 1, false, "Updated body");

        verify(ps).setObject(1, false);
        verify(ps).setObject(2, "Updated body");
        verify(ps).setObject(3, 1);
    }

    @Test
    void documentRequirements_mapsRows() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("doc_type")).thenReturn("FIR_COPY");
        when(rs.getBoolean("required")).thenReturn(true);

        List<DocumentRequirement> result = dao.documentRequirements(conn);

        assertEquals("FIR_COPY", result.get(0).getDocType());
    }

    @Test
    void insertDocumentRequirement_returnsGeneratedKey() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(ps);
        ResultSet keys = mock(ResultSet.class);
        when(ps.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(true);
        when(keys.getLong(1)).thenReturn(8L);

        DocumentRequirement d = new DocumentRequirement();
        d.setClaimType("MOTOR");
        d.setDocType("FIR_COPY");
        d.setRequired(true);

        assertEquals(8L, dao.insertDocumentRequirement(conn, d));
    }

    @Test
    void deleteDocumentRequirement_bindsId() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        dao.deleteDocumentRequirement(conn, 4);

        verify(ps).setObject(1, 4);
    }
}

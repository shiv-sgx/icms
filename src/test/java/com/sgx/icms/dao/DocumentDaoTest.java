package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sgx.icms.domain.ClaimDocument;
import com.sgx.icms.domain.DocumentRequirement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DocumentDaoTest {

    private final DocumentDao dao = new DocumentDao();

    @Test
    void findByClaim_mapsRows() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("doc_type")).thenReturn("FIR_COPY");

        List<ClaimDocument> result = dao.findByClaim(conn, 1L);

        assertEquals("FIR_COPY", result.get(0).getDocType());
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

        assertNull(dao.findById(conn, 1L));
    }

    @Test
    void insert_defaultsStatuses_andReturnsGeneratedKey() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(ps);
        ResultSet keys = mock(ResultSet.class);
        when(ps.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(true);
        when(keys.getLong(1)).thenReturn(11L);

        ClaimDocument d = new ClaimDocument();
        d.setClaimId(1L);
        d.setDocType("FIR_COPY");
        // uploadStatus/verificationStatus left null -> default to PENDING

        assertEquals(11L, dao.insert(conn, d));
        verify(ps).setObject(5, "PENDING");
        verify(ps).setObject(6, "PENDING");
    }

    @Test
    void markUploaded_bindsFileNameFilePathId() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        dao.markUploaded(conn, 1L, "fir.pdf", "/uploads/fir.pdf");

        verify(ps).setObject(1, "fir.pdf");
        verify(ps).setObject(2, "/uploads/fir.pdf");
        verify(ps).setObject(3, 1L);
    }

    @Test
    void findRequirements_bindsTypeAndSubtype() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        dao.findRequirements(conn, "MOTOR", "ACCIDENT");

        verify(ps).setObject(1, "MOTOR");
        verify(ps).setObject(2, "ACCIDENT");
    }
}

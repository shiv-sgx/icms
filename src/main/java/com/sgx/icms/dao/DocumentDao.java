package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.sgx.icms.db.Db;
import com.sgx.icms.db.RowMapper;
import com.sgx.icms.domain.ClaimDocument;
import com.sgx.icms.domain.DocumentRequirement;

public class DocumentDao {

    private static final RowMapper<ClaimDocument> DOC_MAPPER = DocumentDao::mapDoc;
    private static final RowMapper<DocumentRequirement> REQ_MAPPER = DocumentDao::mapReq;

    private static ClaimDocument mapDoc(ResultSet rs) throws SQLException {
        ClaimDocument d = new ClaimDocument();
        d.setId(rs.getLong("id"));
        d.setClaimId(rs.getLong("claim_id"));
        d.setDocType(rs.getString("doc_type"));
        d.setFileName(rs.getString("file_name"));
        d.setFilePath(rs.getString("file_path"));
        d.setUploadStatus(rs.getString("upload_status"));
        d.setVerificationStatus(rs.getString("verification_status"));
        d.setUploadedAt(rs.getObject("uploaded_at", java.time.LocalDateTime.class));
        return d;
    }

    private static DocumentRequirement mapReq(ResultSet rs) throws SQLException {
        DocumentRequirement r = new DocumentRequirement();
        r.setId(rs.getInt("id"));
        r.setClaimType(rs.getString("claim_type"));
        r.setClaimSubtype(rs.getString("claim_subtype"));
        r.setDocType(rs.getString("doc_type"));
        r.setRequired(rs.getBoolean("required"));
        return r;
    }

    public List<ClaimDocument> findByClaim(Connection conn, long claimId) {
        return Db.query(conn,
            "SELECT id, claim_id, doc_type, file_name, file_path, upload_status, verification_status, uploaded_at "
          + "FROM claim_documents WHERE claim_id = ? ORDER BY id", DOC_MAPPER, claimId);
    }

    public ClaimDocument findById(Connection conn, long id) {
        return Db.queryOne(conn,
            "SELECT id, claim_id, doc_type, file_name, file_path, upload_status, verification_status, uploaded_at "
          + "FROM claim_documents WHERE id = ?", DOC_MAPPER, id);
    }

    public long insert(Connection conn, ClaimDocument d) {
        return Db.insert(conn,
            "INSERT INTO claim_documents (claim_id, doc_type, file_name, file_path, upload_status, verification_status) "
          + "VALUES (?,?,?,?,?,?)",
            d.getClaimId(), d.getDocType(), d.getFileName(), d.getFilePath(),
            d.getUploadStatus() == null ? "PENDING" : d.getUploadStatus(),
            d.getVerificationStatus() == null ? "PENDING" : d.getVerificationStatus());
    }

    /** Marks an existing required-doc slot (or any doc) as uploaded. */
    public void markUploaded(Connection conn, long docId, String fileName, String filePath) {
        Db.update(conn,
            "UPDATE claim_documents SET file_name = ?, file_path = ?, upload_status = 'UPLOADED', "
          + "verification_status = 'UNDER_REVIEW', uploaded_at = NOW() WHERE id = ?",
            fileName, filePath, docId);
    }

    public List<DocumentRequirement> findRequirements(Connection conn, String claimType, String subtype) {
        // Match on type; subtype match is best-effort (subtype-specific OR type-wide rows).
        return Db.query(conn,
            "SELECT id, claim_type, claim_subtype, doc_type, required FROM document_requirements "
          + "WHERE claim_type = ? AND (claim_subtype = ? OR claim_subtype IS NULL) ORDER BY id",
            REQ_MAPPER, claimType, subtype);
    }
}

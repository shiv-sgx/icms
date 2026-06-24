package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.sgx.icms.db.Db;
import com.sgx.icms.db.RowMapper;
import com.sgx.icms.domain.ApprovalThreshold;
import com.sgx.icms.domain.DocumentRequirement;
import com.sgx.icms.domain.NotificationTemplate;
import com.sgx.icms.domain.SlaConfig;

/** Read/write access to admin-managed configuration. */
public class ConfigDao {

    private static final RowMapper<ApprovalThreshold> THRESHOLD_MAPPER = ConfigDao::mapThreshold;
    private static final RowMapper<SlaConfig> SLA_MAPPER = ConfigDao::mapSla;
    private static final RowMapper<NotificationTemplate> TEMPLATE_MAPPER = ConfigDao::mapTemplate;
    private static final RowMapper<DocumentRequirement> DOCREQ_MAPPER = ConfigDao::mapDocReq;

    private static ApprovalThreshold mapThreshold(ResultSet rs) throws SQLException {
        ApprovalThreshold t = new ApprovalThreshold();
        t.setId(rs.getInt("id"));
        t.setLevel(rs.getString("level"));
        t.setLabel(rs.getString("label"));
        t.setMinAmount(rs.getBigDecimal("min_amount"));
        t.setMaxAmount(rs.getBigDecimal("max_amount"));
        return t;
    }

    private static SlaConfig mapSla(ResultSet rs) throws SQLException {
        SlaConfig s = new SlaConfig();
        s.setId(rs.getInt("id"));
        s.setStage(rs.getString("stage"));
        s.setHours(rs.getInt("hours"));
        return s;
    }

    private static NotificationTemplate mapTemplate(ResultSet rs) throws SQLException {
        NotificationTemplate t = new NotificationTemplate();
        t.setId(rs.getInt("id"));
        t.setName(rs.getString("name"));
        t.setChannel(rs.getString("channel"));
        t.setActive(rs.getBoolean("active"));
        t.setBody(rs.getString("body"));
        return t;
    }

    private static DocumentRequirement mapDocReq(ResultSet rs) throws SQLException {
        DocumentRequirement d = new DocumentRequirement();
        d.setId(rs.getInt("id"));
        d.setClaimType(rs.getString("claim_type"));
        d.setClaimSubtype(rs.getString("claim_subtype"));
        d.setDocType(rs.getString("doc_type"));
        d.setRequired(rs.getBoolean("required"));
        return d;
    }

    /* ---- approval thresholds ---- */
    public List<ApprovalThreshold> approvalThresholds(Connection conn) {
        return Db.query(conn,
            "SELECT id, level, label, min_amount, max_amount FROM approval_thresholds ORDER BY min_amount",
            THRESHOLD_MAPPER);
    }

    public void updateThreshold(Connection conn, int id, java.math.BigDecimal min, java.math.BigDecimal max) {
        Db.update(conn, "UPDATE approval_thresholds SET min_amount = ?, max_amount = ? WHERE id = ?",
                min, max, id);
    }

    /* ---- SLA config ---- */
    public List<SlaConfig> slaConfigs(Connection conn) {
        return Db.query(conn, "SELECT id, stage, hours FROM sla_config ORDER BY id", SLA_MAPPER);
    }

    public void updateSla(Connection conn, int id, int hours) {
        Db.update(conn, "UPDATE sla_config SET hours = ? WHERE id = ?", hours, id);
    }

    /* ---- notification templates ---- */
    public List<NotificationTemplate> templates(Connection conn) {
        return Db.query(conn, "SELECT id, name, channel, active, body FROM notification_templates ORDER BY id",
                TEMPLATE_MAPPER);
    }

    public void updateTemplate(Connection conn, int id, boolean active, String body) {
        Db.update(conn, "UPDATE notification_templates SET active = ?, body = ? WHERE id = ?", active, body, id);
    }

    /* ---- document requirements ---- */
    public List<DocumentRequirement> documentRequirements(Connection conn) {
        return Db.query(conn,
            "SELECT id, claim_type, claim_subtype, doc_type, required FROM document_requirements "
          + "ORDER BY claim_type, claim_subtype, id", DOCREQ_MAPPER);
    }

    public long insertDocumentRequirement(Connection conn, DocumentRequirement d) {
        return Db.insert(conn,
            "INSERT INTO document_requirements (claim_type, claim_subtype, doc_type, required) VALUES (?,?,?,?)",
            d.getClaimType(), d.getClaimSubtype(), d.getDocType(), d.isRequired());
    }

    public void deleteDocumentRequirement(Connection conn, int id) {
        Db.update(conn, "DELETE FROM document_requirements WHERE id = ?", id);
    }
}

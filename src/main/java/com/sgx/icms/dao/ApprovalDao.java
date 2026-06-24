package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.sgx.icms.db.Db;
import com.sgx.icms.db.RowMapper;
import com.sgx.icms.domain.Approval;

/** Persistence for the {@code approvals} chain. */
public class ApprovalDao {

    private static final String SELECT =
            "SELECT a.*, u.full_name AS approver_name FROM approvals a "
          + "LEFT JOIN users u ON u.id = a.approver_id ";

    private static final RowMapper<Approval> MAPPER = ApprovalDao::map;

    private static Approval map(ResultSet rs) throws SQLException {
        Approval a = new Approval();
        a.setId(rs.getLong("id"));
        a.setClaimId(rs.getLong("claim_id"));
        a.setLevel(rs.getString("level"));
        long aid = rs.getLong("approver_id");
        a.setApproverId(rs.wasNull() ? null : aid);
        a.setApproverRole(rs.getString("approver_role"));
        a.setDecision(rs.getString("decision"));
        a.setRemarks(rs.getString("remarks"));
        a.setDecidedAt(rs.getObject("decided_at", java.time.LocalDateTime.class));
        a.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        a.setApproverName(rs.getString("approver_name"));
        return a;
    }

    public List<Approval> findByClaim(Connection conn, long claimId) {
        return Db.query(conn, SELECT + "WHERE a.claim_id = ? ORDER BY a.level, a.id", MAPPER, claimId);
    }

    /** Lowest-level pending approval for a claim (the next decision needed), or null. */
    public Approval findNextPending(Connection conn, long claimId) {
        return Db.queryOne(conn,
            SELECT + "WHERE a.claim_id = ? AND a.decision = 'PENDING' ORDER BY a.level, a.id LIMIT 1",
            MAPPER, claimId);
    }

    public long insert(Connection conn, Approval a) {
        return Db.insert(conn,
            "INSERT INTO approvals (claim_id, level, approver_id, approver_role, decision, remarks, decided_at) "
          + "VALUES (?,?,?,?,?,?,?)",
            a.getClaimId(), a.getLevel(), a.getApproverId(), a.getApproverRole(),
            a.getDecision() == null ? Approval.PENDING : a.getDecision(),
            a.getRemarks(), a.getDecidedAt());
    }

    public void decide(Connection conn, long approvalId, long approverId, String decision, String remarks) {
        Db.update(conn,
            "UPDATE approvals SET approver_id = ?, decision = ?, remarks = ?, decided_at = NOW() WHERE id = ?",
            approverId, decision, remarks, approvalId);
    }

    public long countPending(Connection conn, long claimId) {
        return Db.queryLong(conn,
            "SELECT COUNT(*) FROM approvals WHERE claim_id = ? AND decision = 'PENDING'", claimId);
    }
}

package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sgx.icms.db.Db;
import com.sgx.icms.db.RowMapper;
import com.sgx.icms.domain.Settlement;

/** Persistence for {@code settlements}. */
public class SettlementDao {

    private static final RowMapper<Settlement> MAPPER = SettlementDao::map;

    private static Settlement map(ResultSet rs) throws SQLException {
        Settlement s = new Settlement();
        s.setId(rs.getLong("id"));
        s.setClaimId(rs.getLong("claim_id"));
        s.setFinalAmount(rs.getBigDecimal("final_amount"));
        s.setJustification(rs.getString("justification"));
        s.setPaymentMethod(rs.getString("payment_method"));
        s.setAccountHolder(rs.getString("account_holder"));
        s.setBankName(rs.getString("bank_name"));
        s.setAccountNumber(rs.getString("account_number"));
        s.setIfscCode(rs.getString("ifsc_code"));
        s.setStatus(rs.getString("status"));
        long ab = rs.getLong("approved_by");
        s.setApprovedBy(rs.wasNull() ? null : ab);
        s.setApprovalDate(rs.getObject("approval_date", java.time.LocalDateTime.class));
        s.setPaymentInitiatedAt(rs.getObject("payment_initiated_at", java.time.LocalDateTime.class));
        s.setPaymentConfirmedAt(rs.getObject("payment_confirmed_at", java.time.LocalDateTime.class));
        s.setClosedAt(rs.getObject("closed_at", java.time.LocalDateTime.class));
        s.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        return s;
    }

    public Settlement findByClaim(Connection conn, long claimId) {
        return Db.queryOne(conn,
            "SELECT * FROM settlements WHERE claim_id = ?", MAPPER, claimId);
    }

    public long insert(Connection conn, Settlement s) {
        return Db.insert(conn,
            "INSERT INTO settlements (claim_id, final_amount, justification, payment_method, account_holder, "
          + "bank_name, account_number, ifsc_code, status, approved_by, approval_date) "
          + "VALUES (?,?,?,?,?,?,?,?,?,?,NOW())",
            s.getClaimId(), s.getFinalAmount(), s.getJustification(), s.getPaymentMethod(),
            s.getAccountHolder(), s.getBankName(), s.getAccountNumber(), s.getIfscCode(),
            s.getStatus() == null ? Settlement.AUTHORIZED : s.getStatus(), s.getApprovedBy());
    }

    public void updateAmount(Connection conn, long claimId, java.math.BigDecimal amount, String justification) {
        Db.update(conn, "UPDATE settlements SET final_amount = ?, justification = ? WHERE claim_id = ?",
                amount, justification, claimId);
    }

    /** Advances the payment status and stamps the matching timestamp column. */
    public void advance(Connection conn, long claimId, String status) {
        String stampCol = null;
        switch (status) {
            case Settlement.PAYMENT_INITIATED: stampCol = "payment_initiated_at"; break;
            case Settlement.PAYMENT_CONFIRMED: stampCol = "payment_confirmed_at"; break;
            case Settlement.CLOSED: stampCol = "closed_at"; break;
            default: break;
        }
        if (stampCol != null) {
            Db.update(conn, "UPDATE settlements SET status = ?, " + stampCol + " = NOW() WHERE claim_id = ?",
                    status, claimId);
        } else {
            Db.update(conn, "UPDATE settlements SET status = ? WHERE claim_id = ?", status, claimId);
        }
    }
}

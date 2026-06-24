package com.sgx.icms.dao;

import java.sql.Connection;
import java.util.List;

import com.sgx.icms.db.Db;
import com.sgx.icms.db.RowMapper;

/**
 * Read-only aggregate queries powering the manager Reports &amp; Analytics screens.
 * Each method returns rows as {@code String[]} so they map straight onto a CSV/table.
 */
public class ReportDao {

    public List<String[]> claimsByStatus(Connection conn) {
        return Db.query(conn,
            "SELECT status, COUNT(*) AS n, COALESCE(SUM(estimated_loss),0) AS total "
          + "FROM claims GROUP BY status ORDER BY n DESC", row(3));
    }

    public List<String[]> claimsByType(Connection conn) {
        return Db.query(conn,
            "SELECT claim_type, COUNT(*) AS n, COALESCE(SUM(estimated_loss),0) AS total "
          + "FROM claims GROUP BY claim_type ORDER BY n DESC", row(3));
    }

    public List<String[]> slaCompliance(Connection conn) {
        return Db.query(conn,
            "SELECT CASE "
          + " WHEN sla_due_date IS NULL THEN 'No SLA Set' "
          + " WHEN sla_due_date < CURDATE() AND status NOT IN ('SETTLED','CLOSED','REJECTED','WITHDRAWN') THEN 'Breached' "
          + " ELSE 'Within SLA' END AS bucket, COUNT(*) AS n "
          + "FROM claims GROUP BY bucket ORDER BY n DESC", row(2));
    }

    public List<String[]> settlementTat(Connection conn) {
        return Db.query(conn,
            "SELECT 'Settlements Confirmed' AS metric, COUNT(*) AS v FROM settlements WHERE payment_confirmed_at IS NOT NULL "
          + "UNION ALL "
          + "SELECT 'Avg TAT (days, filed→confirmed)', "
          + " COALESCE(ROUND(AVG(DATEDIFF(s.payment_confirmed_at, c.filed_at)),1),0) "
          + " FROM settlements s JOIN claims c ON c.id = s.claim_id WHERE s.payment_confirmed_at IS NOT NULL "
          + "UNION ALL "
          + "SELECT 'Total Settled Amount', COALESCE(SUM(final_amount),0) FROM settlements", row(2));
    }

    public List<String[]> fraudWatch(Connection conn) {
        return Db.query(conn,
            "SELECT claim_no, claim_type, risk_level, CAST(fraud_score AS CHAR), status "
          + "FROM claims WHERE fraud_score >= 60 OR risk_level = 'HIGH' "
          + "ORDER BY fraud_score DESC", row(5));
    }

    public List<String[]> agentPerformance(Connection conn) {
        return Db.query(conn,
            "SELECT u.full_name, "
          + " COUNT(c.id) AS total, "
          + " SUM(c.status IN ('SETTLED','CLOSED')) AS settled, "
          + " SUM(c.status = 'PENDING_APPROVAL') AS pending "
          + "FROM users u JOIN claims c ON c.agent_id = u.id "
          + "GROUP BY u.id, u.full_name ORDER BY total DESC", row(4));
    }

    /** Maps the first {@code n} columns of a row to a String[]. */
    private static RowMapper<String[]> row(int n) {
        return rs -> {
            String[] out = new String[n];
            for (int i = 0; i < n; i++) {
                out[i] = rs.getString(i + 1);
            }
            return out;
        };
    }
}

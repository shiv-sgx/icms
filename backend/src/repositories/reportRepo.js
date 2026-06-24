'use strict';

const { knex } = require('../db/knex');

/**
 * Read-only aggregate queries for the manager Reports & Analytics — Knex/raw port
 * of ReportDao. Each returns an array of string-arrays (rows) so they map straight
 * onto a table / CSV.
 */

async function rawRows(sql, n) {
  const [rows] = await knex.raw(sql);
  return rows.map((r) => {
    const vals = Object.values(r);
    return Array.from({ length: n }, (_, i) => (vals[i] == null ? '' : String(vals[i])));
  });
}

const claimsByStatus = () =>
  rawRows(
    "SELECT status, COUNT(*) AS n, COALESCE(SUM(estimated_loss),0) AS total FROM claims GROUP BY status ORDER BY n DESC",
    3
  );

const claimsByType = () =>
  rawRows(
    "SELECT claim_type, COUNT(*) AS n, COALESCE(SUM(estimated_loss),0) AS total FROM claims GROUP BY claim_type ORDER BY n DESC",
    3
  );

const slaCompliance = () =>
  rawRows(
    "SELECT CASE " +
      " WHEN sla_due_date IS NULL THEN 'No SLA Set' " +
      " WHEN sla_due_date < CURDATE() AND status NOT IN ('SETTLED','CLOSED','REJECTED','WITHDRAWN') THEN 'Breached' " +
      " ELSE 'Within SLA' END AS bucket, COUNT(*) AS n " +
      "FROM claims GROUP BY bucket ORDER BY n DESC",
    2
  );

const settlementTat = () =>
  rawRows(
    "SELECT 'Settlements Confirmed' AS metric, COUNT(*) AS v FROM settlements WHERE payment_confirmed_at IS NOT NULL " +
      "UNION ALL " +
      "SELECT 'Avg TAT (days, filed->confirmed)', COALESCE(ROUND(AVG(DATEDIFF(s.payment_confirmed_at, c.filed_at)),1),0) " +
      " FROM settlements s JOIN claims c ON c.id = s.claim_id WHERE s.payment_confirmed_at IS NOT NULL " +
      "UNION ALL " +
      "SELECT 'Total Settled Amount', COALESCE(SUM(final_amount),0) FROM settlements",
    2
  );

const fraudWatch = () =>
  rawRows(
    "SELECT claim_no, claim_type, risk_level, CAST(fraud_score AS CHAR), status " +
      "FROM claims WHERE fraud_score >= 60 OR risk_level = 'HIGH' ORDER BY fraud_score DESC",
    5
  );

const agentPerformance = () =>
  rawRows(
    "SELECT u.full_name, COUNT(c.id) AS total, " +
      " SUM(c.status IN ('SETTLED','CLOSED')) AS settled, " +
      " SUM(c.status = 'PENDING_APPROVAL') AS pending " +
      "FROM users u JOIN claims c ON c.agent_id = u.id " +
      "GROUP BY u.id, u.full_name ORDER BY total DESC",
    4
  );

module.exports = {
  claimsByStatus,
  claimsByType,
  slaCompliance,
  settlementTat,
  fraudWatch,
  agentPerformance,
};

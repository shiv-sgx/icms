package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sgx.icms.db.Db;
import com.sgx.icms.db.RowMapper;
import com.sgx.icms.domain.Claim;

/**
 * Persistence for {@code claims}. The SELECT joins the policy number and the agent/
 * surveyor display names so list/detail views avoid N+1 lookups.
 */
public class ClaimDao {

    /** c.* keeps it simple (no column clash since only claims.* is selected). */
    static final String SELECT =
            "SELECT c.*, p.policy_no AS policy_no, "
          + "ag.full_name AS agent_name, sv.full_name AS surveyor_name "
          + "FROM claims c "
          + "JOIN policies p ON p.id = c.policy_id "
          + "LEFT JOIN users ag ON ag.id = c.agent_id "
          + "LEFT JOIN users sv ON sv.id = c.surveyor_id ";

    static final RowMapper<Claim> MAPPER = ClaimDao::map;

    static Claim map(ResultSet rs) throws SQLException {
        Claim c = new Claim();
        c.setId(rs.getLong("id"));
        c.setClaimNo(rs.getString("claim_no"));
        c.setPolicyId(rs.getLong("policy_id"));
        c.setPolicyholderId(rs.getLong("policyholder_id"));
        c.setClaimantName(rs.getString("claimant_name"));
        c.setClaimType(rs.getString("claim_type"));
        c.setClaimSubtype(rs.getString("claim_subtype"));
        c.setIncidentDate(rs.getObject("incident_date", java.time.LocalDate.class));
        c.setIncidentTime(rs.getObject("incident_time", java.time.LocalTime.class));
        c.setIncidentLocation(rs.getString("incident_location"));
        c.setCity(rs.getString("city"));
        c.setState(rs.getString("state"));
        c.setPinCode(rs.getString("pin_code"));
        c.setDescription(rs.getString("description"));
        c.setEstimatedLoss(rs.getBigDecimal("estimated_loss"));
        c.setVehicleRegNo(rs.getString("vehicle_reg_no"));
        c.setFirNumber(rs.getString("fir_number"));
        c.setPoliceStation(rs.getString("police_station"));
        c.setHospitalName(rs.getString("hospital_name"));
        c.setWorkshopName(rs.getString("workshop_name"));
        c.setThirdParty(rs.getString("third_party"));
        c.setStatus(rs.getString("status"));
        long agentId = rs.getLong("agent_id");
        c.setAgentId(rs.wasNull() ? null : agentId);
        long surveyorId = rs.getLong("surveyor_id");
        c.setSurveyorId(rs.wasNull() ? null : surveyorId);
        c.setRiskLevel(rs.getString("risk_level"));
        c.setFraudScore(rs.getInt("fraud_score"));
        c.setInternalNotes(rs.getString("internal_notes"));
        c.setSlaDueDate(rs.getObject("sla_due_date", java.time.LocalDate.class));
        c.setFiledAt(rs.getObject("filed_at", java.time.LocalDateTime.class));
        c.setUpdatedAt(rs.getObject("updated_at", java.time.LocalDateTime.class));
        c.setPolicyNo(rs.getString("policy_no"));
        c.setAgentName(rs.getString("agent_name"));
        c.setSurveyorName(rs.getString("surveyor_name"));
        return c;
    }

    public Claim findById(Connection conn, long id) {
        return Db.queryOne(conn, SELECT + "WHERE c.id = ?", MAPPER, id);
    }

    public List<Claim> findByPolicyholder(Connection conn, long phId, int limit, int offset) {
        return Db.query(conn, SELECT + "WHERE c.policyholder_id = ? ORDER BY c.filed_at DESC LIMIT ? OFFSET ?",
                MAPPER, phId, limit, offset);
    }

    public long countByPolicyholder(Connection conn, long phId) {
        return Db.queryLong(conn, "SELECT COUNT(*) FROM claims WHERE policyholder_id = ?", phId);
    }

    /** Count a policyholder's claims whose status is in the given set (dashboard KPIs). */
    public long countByPolicyholderInStatuses(Connection conn, long phId, List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return 0;
        }
        String placeholders = String.join(",", Collections.nCopies(statuses.size(), "?"));
        Object[] params = new Object[statuses.size() + 1];
        params[0] = phId;
        for (int i = 0; i < statuses.size(); i++) {
            params[i + 1] = statuses.get(i);
        }
        return Db.queryLong(conn,
                "SELECT COUNT(*) FROM claims WHERE policyholder_id = ? AND status IN (" + placeholders + ")",
                params);
    }

    /** Generates the next claim number CLM-YYYY-#### for the given year. */
    public String nextClaimNo(Connection conn, int year) {
        long seq = Db.queryLong(conn, "SELECT IFNULL(MAX(id),0) + 1 FROM claims");
        return String.format("CLM-%d-%04d", year, seq);
    }

    public long insert(Connection conn, Claim c) {
        return Db.insert(conn,
            "INSERT INTO claims (claim_no, policy_id, policyholder_id, claimant_name, claim_type, "
          + "claim_subtype, incident_date, incident_time, incident_location, city, state, pin_code, "
          + "description, estimated_loss, vehicle_reg_no, fir_number, police_station, hospital_name, "
          + "workshop_name, third_party, status, risk_level, fraud_score) "
          + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            c.getClaimNo(), c.getPolicyId(), c.getPolicyholderId(), c.getClaimantName(), c.getClaimType(),
            c.getClaimSubtype(), c.getIncidentDate(), c.getIncidentTime(), c.getIncidentLocation(),
            c.getCity(), c.getState(), c.getPinCode(), c.getDescription(), c.getEstimatedLoss(),
            c.getVehicleRegNo(), c.getFirNumber(), c.getPoliceStation(), c.getHospitalName(),
            c.getWorkshopName(), c.getThirdParty(), c.getStatus(),
            c.getRiskLevel() == null ? "LOW" : c.getRiskLevel(), c.getFraudScore());
    }

    public void updateStatus(Connection conn, long claimId, String status) {
        Db.update(conn, "UPDATE claims SET status = ? WHERE id = ?", status, claimId);
    }

    /* ----------------------------- surveyor queries ----------------------------- */

    public List<Claim> findBySurveyor(Connection conn, long surveyorId, int limit, int offset) {
        return Db.query(conn, SELECT + "WHERE c.surveyor_id = ? ORDER BY c.filed_at DESC LIMIT ? OFFSET ?",
                MAPPER, surveyorId, limit, offset);
    }

    public long countBySurveyor(Connection conn, long surveyorId) {
        return Db.queryLong(conn, "SELECT COUNT(*) FROM claims WHERE surveyor_id = ?", surveyorId);
    }

    public long countBySurveyorInStatuses(Connection conn, long surveyorId, List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return 0;
        }
        String placeholders = String.join(",", Collections.nCopies(statuses.size(), "?"));
        Object[] params = new Object[statuses.size() + 1];
        params[0] = surveyorId;
        for (int i = 0; i < statuses.size(); i++) {
            params[i + 1] = statuses.get(i);
        }
        return Db.queryLong(conn,
                "SELECT COUNT(*) FROM claims WHERE surveyor_id = ? AND status IN (" + placeholders + ")",
                params);
    }

    /* ----------------------------- agent/manager queries ----------------------------- */

    /** Filtered + paginated claim list (status/type/free-text). All params optional. */
    public List<Claim> findFiltered(Connection conn, String status, String type, String search,
                                    int limit, int offset) {
        StringBuilder sql = new StringBuilder(SELECT);
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, status, type, search);
        sql.append("ORDER BY c.filed_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return Db.query(conn, sql.toString(), MAPPER, params.toArray());
    }

    public long countFiltered(Connection conn, String status, String type, String search) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM claims c ");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, status, type, search);
        return Db.queryLong(conn, sql.toString(), params.toArray());
    }

    /** status -> count, for dashboard KPIs. */
    public Map<String, Long> countByStatus(Connection conn) {
        Map<String, Long> out = new LinkedHashMap<>();
        Db.query(conn, "SELECT status, COUNT(*) AS n FROM claims GROUP BY status", rs -> {
            out.put(rs.getString("status"), rs.getLong("n"));
            return null;
        });
        return out;
    }

    /** Claims awaiting an agent action (worklist): submitted/under-review/assessed. */
    public List<Claim> worklist(Connection conn, int limit) {
        return Db.query(conn, SELECT
              + "WHERE c.status IN ('SUBMITTED','UNDER_REVIEW','UNDER_ASSESSMENT','APPROVED') "
              + "ORDER BY c.filed_at ASC LIMIT ?", MAPPER, limit);
    }

    public void assignSurveyor(Connection conn, long claimId, long surveyorId, Long agentId, String status) {
        Db.update(conn,
            "UPDATE claims SET surveyor_id = ?, agent_id = COALESCE(agent_id, ?), status = ? WHERE id = ?",
            surveyorId, agentId, status, claimId);
    }

    public void acknowledge(Connection conn, long claimId, long agentId, String status) {
        Db.update(conn, "UPDATE claims SET agent_id = ?, status = ? WHERE id = ?", agentId, status, claimId);
    }

    public void updateInternalNotes(Connection conn, long claimId, String notes) {
        Db.update(conn, "UPDATE claims SET internal_notes = ? WHERE id = ?", notes, claimId);
    }

    private static void appendFilters(StringBuilder sql, List<Object> params,
                                      String status, String type, String search) {
        List<String> clauses = new ArrayList<>();
        if (status != null && !status.isEmpty()) {
            clauses.add("c.status = ?");
            params.add(status);
        }
        if (type != null && !type.isEmpty()) {
            clauses.add("c.claim_type = ?");
            params.add(type);
        }
        if (search != null && !search.trim().isEmpty()) {
            clauses.add("(c.claim_no LIKE ? OR c.claimant_name LIKE ?)");
            String like = "%" + search.trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (!clauses.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", clauses)).append(' ');
        }
    }
}

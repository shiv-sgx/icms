package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.sgx.icms.db.Db;
import com.sgx.icms.db.RowMapper;
import com.sgx.icms.domain.Assessment;
import com.sgx.icms.domain.AssessmentComponent;

/** Persistence for {@code assessments} and {@code assessment_components}. */
public class AssessmentDao {

    private static final String SELECT =
            "SELECT a.*, u.full_name AS surveyor_name FROM assessments a "
          + "LEFT JOIN users u ON u.id = a.surveyor_id ";

    private static final RowMapper<Assessment> MAPPER = AssessmentDao::map;
    private static final RowMapper<AssessmentComponent> COMP_MAPPER = AssessmentDao::mapComp;

    private static Assessment map(ResultSet rs) throws SQLException {
        Assessment a = new Assessment();
        a.setId(rs.getLong("id"));
        a.setClaimId(rs.getLong("claim_id"));
        long sid = rs.getLong("surveyor_id");
        a.setSurveyorId(rs.wasNull() ? null : sid);
        a.setVisitDate(rs.getObject("visit_date", java.time.LocalDate.class));
        a.setVisitTime(rs.getObject("visit_time", java.time.LocalTime.class));
        a.setSiteObservations(rs.getString("site_observations"));
        a.setReportRefNo(rs.getString("report_ref_no"));
        a.setGrossAssessed(rs.getBigDecimal("gross_assessed"));
        a.setPolicyDeductible(rs.getBigDecimal("policy_deductible"));
        a.setDepreciationPct(rs.getBigDecimal("depreciation_pct"));
        a.setDepreciationAmt(rs.getBigDecimal("depreciation_amt"));
        a.setSalvageValue(rs.getBigDecimal("salvage_value"));
        a.setNetPayable(rs.getBigDecimal("net_payable"));
        a.setRecommendation(rs.getString("recommendation"));
        a.setRemarks(rs.getString("remarks"));
        a.setStatus(rs.getString("status"));
        a.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        a.setSurveyorName(rs.getString("surveyor_name"));
        return a;
    }

    private static AssessmentComponent mapComp(ResultSet rs) throws SQLException {
        AssessmentComponent c = new AssessmentComponent();
        c.setId(rs.getLong("id"));
        c.setAssessmentId(rs.getLong("assessment_id"));
        c.setComponent(rs.getString("component"));
        c.setSeverity(rs.getString("severity"));
        c.setRepairCost(rs.getBigDecimal("repair_cost"));
        c.setReplaceFlag(rs.getBoolean("replace_flag"));
        return c;
    }

    /** Latest assessment for a claim, or null. */
    public Assessment findByClaim(Connection conn, long claimId) {
        return Db.queryOne(conn, SELECT + "WHERE a.claim_id = ? ORDER BY a.id DESC LIMIT 1", MAPPER, claimId);
    }

    public Assessment findById(Connection conn, long id) {
        return Db.queryOne(conn, SELECT + "WHERE a.id = ?", MAPPER, id);
    }

    public List<AssessmentComponent> findComponents(Connection conn, long assessmentId) {
        return Db.query(conn,
            "SELECT id, assessment_id, component, severity, repair_cost, replace_flag "
          + "FROM assessment_components WHERE assessment_id = ? ORDER BY id", COMP_MAPPER, assessmentId);
    }

    /* ---- writes (used by the surveyor portal, Phase 3) ---- */

    public long insert(Connection conn, Assessment a) {
        return Db.insert(conn,
            "INSERT INTO assessments (claim_id, surveyor_id, visit_date, visit_time, site_observations, "
          + "report_ref_no, gross_assessed, policy_deductible, depreciation_pct, depreciation_amt, "
          + "salvage_value, net_payable, recommendation, remarks, status) "
          + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            a.getClaimId(), a.getSurveyorId(), a.getVisitDate(), a.getVisitTime(), a.getSiteObservations(),
            a.getReportRefNo(), a.getGrossAssessed(), a.getPolicyDeductible(), a.getDepreciationPct(),
            a.getDepreciationAmt(), a.getSalvageValue(), a.getNetPayable(), a.getRecommendation(),
            a.getRemarks(), a.getStatus());
    }

    public void insertComponent(Connection conn, AssessmentComponent c) {
        Db.insert(conn,
            "INSERT INTO assessment_components (assessment_id, component, severity, repair_cost, replace_flag) "
          + "VALUES (?,?,?,?,?)",
            c.getAssessmentId(), c.getComponent(), c.getSeverity(), c.getRepairCost(), c.isReplaceFlag());
    }
}

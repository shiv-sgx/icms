package com.sgx.icms.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sgx.icms.dao.AssessmentDao;
import com.sgx.icms.dao.ClaimDao;
import com.sgx.icms.db.Db;
import com.sgx.icms.domain.Assessment;
import com.sgx.icms.domain.AssessmentComponent;
import com.sgx.icms.domain.AuditLog;
import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.ClaimStatus;
import com.sgx.icms.web.support.Paged;
import com.sgx.icms.web.support.SessionUser;

/**
 * Surveyor use-cases: list claims assigned to the surveyor (ownership-enforced),
 * load a claim's assessment, and submit a damage assessment with a component
 * breakdown. The monetary maths (gross → depreciation → net payable) is computed
 * server-side — never trusted from the client.
 */
public class SurveyorService {

    private static final Logger LOG = LoggerFactory.getLogger(SurveyorService.class);

    private static final List<String> PENDING_STATUSES = Arrays.asList(ClaimStatus.SURVEY_SCHEDULED);
    private static final List<String> ASSESSED_STATUSES = Arrays.asList(
            ClaimStatus.UNDER_ASSESSMENT, ClaimStatus.PENDING_APPROVAL, ClaimStatus.APPROVED,
            ClaimStatus.SETTLEMENT_PROCESSING, ClaimStatus.SETTLED, ClaimStatus.CLOSED);

    private final ClaimDao claimDao = new ClaimDao();
    private final AssessmentDao assessmentDao = new AssessmentDao();
    private final NotificationService notifications = new NotificationService();
    private final AuditService audit = new AuditService();

    public Paged<Claim> assignedClaims(long surveyorId, int page, int size) {
        int offset = (page - 1) * size;
        return Db.withConnection(conn -> {
            long total = claimDao.countBySurveyor(conn, surveyorId);
            List<Claim> items = claimDao.findBySurveyor(conn, surveyorId, size, offset);
            return new Paged<>(items, page, size, total);
        });
    }

    /** [total, pendingSurvey, assessed] for the surveyor dashboard. */
    public long[] counts(long surveyorId) {
        return Db.withConnection(conn -> new long[] {
                claimDao.countBySurveyor(conn, surveyorId),
                claimDao.countBySurveyorInStatuses(conn, surveyorId, PENDING_STATUSES),
                claimDao.countBySurveyorInStatuses(conn, surveyorId, ASSESSED_STATUSES)
        });
    }

    /** Claim only if assigned to this surveyor (authorisation). */
    public Claim getAssignedClaim(long surveyorId, long claimId) {
        Claim c = Db.withConnection(conn -> claimDao.findById(conn, claimId));
        if (c == null || c.getSurveyorId() == null || c.getSurveyorId() != surveyorId) {
            return null;
        }
        return c;
    }

    public Assessment latestAssessment(long claimId) {
        return Db.withConnection(conn -> assessmentDao.findByClaim(conn, claimId));
    }

    public List<AssessmentComponent> components(long assessmentId) {
        return Db.withConnection(conn -> assessmentDao.findComponents(conn, assessmentId));
    }

    /**
     * Persists a submitted assessment. Computes gross (from component repair costs),
     * depreciation amount, and net payable server-side. Moves the claim to
     * UNDER_ASSESSMENT and notifies the handling agent.
     *
     * @throws IllegalArgumentException on invalid input
     * @throws IllegalStateException if the claim isn't assigned to this surveyor / wrong state
     */
    public void submitAssessment(SessionUser surveyor, long claimId, Assessment input,
                                 List<AssessmentComponent> components, String ip) {
        BigDecimal gross = sumRepairCosts(components);
        if (gross.signum() <= 0 && nz(input.getGrossAssessed()).signum() <= 0) {
            throw new IllegalArgumentException("Add at least one component (or a gross assessed amount).");
        }
        if (gross.signum() <= 0) {
            gross = nz(input.getGrossAssessed());
        }

        BigDecimal deductible = nz(input.getPolicyDeductible());
        BigDecimal deprPct = nz(input.getDepreciationPct());
        BigDecimal salvage = nz(input.getSalvageValue());
        BigDecimal deprAmt = gross.multiply(deprPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal net = gross.subtract(deductible).subtract(deprAmt).subtract(salvage);
        if (net.signum() < 0) {
            net = BigDecimal.ZERO;
        }

        input.setGrossAssessed(gross);
        input.setDepreciationAmt(deprAmt);
        input.setNetPayable(net);
        input.setSurveyorId(surveyor.getId());
        input.setClaimId(claimId);
        input.setStatus(Assessment.STATUS_SUBMITTED);

        final BigDecimal netFinal = net;
        Db.inTransaction(conn -> {
            Claim claim = claimDao.findById(conn, claimId);
            if (claim == null || claim.getSurveyorId() == null || claim.getSurveyorId() != surveyor.getId()) {
                throw new IllegalStateException("This claim is not assigned to you.");
            }
            if (ClaimStatus.isTerminal(claim.getStatus())) {
                throw new IllegalStateException("This claim is closed.");
            }
            Assessment existing = assessmentDao.findByClaim(conn, claimId);
            if (existing != null && Assessment.STATUS_SUBMITTED.equals(existing.getStatus())) {
                throw new IllegalStateException("An assessment has already been submitted for this claim.");
            }

            long assessmentId = assessmentDao.insert(conn, input);
            if (components != null) {
                for (AssessmentComponent c : components) {
                    c.setAssessmentId(assessmentId);
                    assessmentDao.insertComponent(conn, c);
                }
            }
            claimDao.updateStatus(conn, claimId, ClaimStatus.UNDER_ASSESSMENT);

            if (claim.getAgentId() != null) {
                notifications.notifyUser(conn, claim.getAgentId(), "ACTION",
                        "Survey report uploaded for claim " + claim.getClaimNo()
                        + " (net payable ₹ " + netFinal + ").");
            }
            writeAudit(conn, surveyor, "ASSESSMENT_SUBMITTED", claim.getClaimNo(), ip);
            LOG.info("Surveyor {} submitted assessment for claim {} (net {})",
                    surveyor.getUsername(), claim.getClaimNo(), netFinal);
            return null;
        });
    }

    private static BigDecimal sumRepairCosts(List<AssessmentComponent> components) {
        BigDecimal sum = BigDecimal.ZERO;
        if (components != null) {
            for (AssessmentComponent c : components) {
                sum = sum.add(nz(c.getRepairCost()));
            }
        }
        return sum;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private void writeAudit(java.sql.Connection conn, SessionUser actor, String action, String entity, String ip) {
        AuditLog a = new AuditLog();
        a.setUserId(actor.getId());
        a.setUsername(actor.getUsername());
        a.setRole(actor.getRole());
        a.setAction(action);
        a.setEntity(entity);
        a.setIpAddress(ip);
        a.setResult(AuditLog.RESULT_SUCCESS);
        audit.record(conn, a);
    }
}

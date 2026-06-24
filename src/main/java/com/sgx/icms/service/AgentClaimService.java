package com.sgx.icms.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sgx.icms.dao.ApprovalDao;
import com.sgx.icms.dao.AssessmentDao;
import com.sgx.icms.dao.ClaimDao;
import com.sgx.icms.dao.CommunicationDao;
import com.sgx.icms.dao.DocumentDao;
import com.sgx.icms.dao.SettlementDao;
import com.sgx.icms.db.Db;
import com.sgx.icms.domain.Assessment;
import com.sgx.icms.domain.AuditLog;
import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.ClaimStatus;
import com.sgx.icms.domain.Roles;
import com.sgx.icms.web.support.ClaimBundle;
import com.sgx.icms.web.support.Paged;
import com.sgx.icms.web.support.SessionUser;

/**
 * Agent/back-office claim operations: filtered listing, full-detail aggregation,
 * acknowledge, internal notes, and forward-for-approval (which builds the approval
 * chain from thresholds). Reused by the manager/surveyor detail views too.
 */
public class AgentClaimService {

    private static final Logger LOG = LoggerFactory.getLogger(AgentClaimService.class);

    private final ClaimDao claimDao = new ClaimDao();
    private final DocumentDao documentDao = new DocumentDao();
    private final CommunicationDao communicationDao = new CommunicationDao();
    private final AssessmentDao assessmentDao = new AssessmentDao();
    private final ApprovalDao approvalDao = new ApprovalDao();
    private final SettlementDao settlementDao = new SettlementDao();
    private final ApprovalService approvalService = new ApprovalService();
    private final NotificationService notifications = new NotificationService();
    private final ClaimService claimService = new ClaimService();
    private final AuditService audit = new AuditService();

    public Paged<Claim> list(String status, String type, String search, int page, int size) {
        int offset = (page - 1) * size;
        return Db.withConnection(conn -> {
            long total = claimDao.countFiltered(conn, status, type, search);
            List<Claim> items = claimDao.findFiltered(conn, status, type, search, size, offset);
            return new Paged<>(items, page, size, total);
        });
    }

    public Map<String, Long> statusCounts() {
        return Db.withConnection(claimDao::countByStatus);
    }

    public List<Claim> worklist(int limit) {
        return Db.withConnection(conn -> claimDao.worklist(conn, limit));
    }

    /** Full detail aggregation for a claim, or {@code null} if it doesn't exist. */
    public ClaimBundle bundle(long claimId) {
        return Db.withConnection(conn -> {
            Claim claim = claimDao.findById(conn, claimId);
            if (claim == null) {
                return null;
            }
            ClaimBundle b = new ClaimBundle();
            b.setClaim(claim);
            b.setDocuments(documentDao.findByClaim(conn, claimId));
            b.setMessages(communicationDao.findByClaim(conn, claimId));
            b.setApprovals(approvalDao.findByClaim(conn, claimId));
            b.setSettlement(settlementDao.findByClaim(conn, claimId));
            Assessment assessment = assessmentDao.findByClaim(conn, claimId);
            b.setAssessment(assessment);
            if (assessment != null) {
                b.setComponents(assessmentDao.findComponents(conn, assessment.getId()));
            }
            b.setTimeline(claimService.timeline(claim));
            return b;
        });
    }

    /** Agent acknowledges a submitted claim (-> UNDER_REVIEW). */
    public void acknowledge(SessionUser agent, long claimId, String ip) {
        Db.inTransaction(conn -> {
            Claim claim = claimDao.findById(conn, claimId);
            if (claim == null) {
                throw new IllegalStateException("Claim not found.");
            }
            if (!ClaimStatus.SUBMITTED.equals(claim.getStatus())) {
                throw new IllegalStateException("Only submitted claims can be acknowledged.");
            }
            claimDao.acknowledge(conn, claimId, agent.getId(), ClaimStatus.UNDER_REVIEW);
            writeAudit(conn, agent, "CLAIM_ACKNOWLEDGED", claim.getClaimNo(), ip);
            return null;
        });
    }

    public void updateNotes(SessionUser agent, long claimId, String notes, String ip) {
        Db.inTransaction(conn -> {
            Claim claim = claimDao.findById(conn, claimId);
            if (claim == null) {
                throw new IllegalStateException("Claim not found.");
            }
            claimDao.updateInternalNotes(conn, claimId, notes);
            writeAudit(conn, agent, "CLAIM_NOTE_UPDATED", claim.getClaimNo(), ip);
            return null;
        });
    }

    /**
     * Forwards a claim for approval: builds the approval chain from the assessed/
     * estimated amount and moves the claim to PENDING_APPROVAL (or APPROVED if it sits
     * fully within agent authority).
     */
    public String forwardForApproval(SessionUser agent, long claimId, String ip) {
        return Db.inTransaction(conn -> {
            Claim claim = claimDao.findById(conn, claimId);
            if (claim == null) {
                throw new IllegalStateException("Claim not found.");
            }
            if (!canForward(claim.getStatus())) {
                throw new IllegalStateException("This claim is not ready to be forwarded for approval.");
            }
            if (approvalDao.countPending(conn, claimId) > 0) {
                throw new IllegalStateException("This claim is already awaiting approval.");
            }

            Assessment assessment = assessmentDao.findByClaim(conn, claimId);
            BigDecimal amount = (assessment != null && assessment.getNetPayable() != null)
                    ? assessment.getNetPayable() : claim.getEstimatedLoss();

            int pending = approvalService.createForwardChain(conn, claimId, agent, amount);
            String newStatus = pending > 0 ? ClaimStatus.PENDING_APPROVAL : ClaimStatus.APPROVED;
            claimDao.updateStatus(conn, claimId, newStatus);

            if (pending > 0) {
                notifications.notifyRole(conn, Roles.MANAGER, "ACTION",
                        "Claim " + claim.getClaimNo() + " is awaiting your approval.");
            }
            writeAudit(conn, agent, "CLAIM_FORWARDED", claim.getClaimNo() + " -> " + newStatus, ip);
            LOG.info("Agent {} forwarded claim {} -> {}", agent.getUsername(), claim.getClaimNo(), newStatus);
            return newStatus;
        });
    }

    private static boolean canForward(String status) {
        return ClaimStatus.UNDER_REVIEW.equals(status)
            || ClaimStatus.UNDER_ASSESSMENT.equals(status)
            || ClaimStatus.SURVEY_SCHEDULED.equals(status)
            || ClaimStatus.ON_HOLD.equals(status);
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

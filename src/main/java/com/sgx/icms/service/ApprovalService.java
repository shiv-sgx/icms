package com.sgx.icms.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sgx.icms.dao.ApprovalDao;
import com.sgx.icms.dao.ClaimDao;
import com.sgx.icms.dao.ConfigDao;
import com.sgx.icms.db.Db;
import com.sgx.icms.domain.Approval;
import com.sgx.icms.domain.ApprovalThreshold;
import com.sgx.icms.domain.AuditLog;
import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.ClaimStatus;
import com.sgx.icms.web.support.SessionUser;

/**
 * Approval-chain logic. When an agent forwards a claim, the chain is built from the
 * configured amount thresholds: the agent's own L1 is recorded as approved, and the
 * manager (L2) / director (L3) levels are added as pending where the amount requires.
 * Managers then decide on the next pending level, which advances the claim.
 */
public class ApprovalService {

    private static final Logger LOG = LoggerFactory.getLogger(ApprovalService.class);

    private final ApprovalDao approvalDao = new ApprovalDao();
    private final ConfigDao configDao = new ConfigDao();
    private final ClaimDao claimDao = new ClaimDao();
    private final NotificationService notifications = new NotificationService();
    private final AuditService audit = new AuditService();

    public List<Approval> forClaim(long claimId) {
        return Db.withConnection(conn -> approvalDao.findByClaim(conn, claimId));
    }

    /**
     * Records a manager's decision on the claim's next pending approval level and
     * advances the claim accordingly:
     * <ul>
     *   <li>APPROVED — if no levels remain pending, the claim is APPROVED; else it
     *       stays PENDING_APPROVAL for the next level.</li>
     *   <li>REJECTED — claim REJECTED.</li>
     *   <li>RETURNED — claim back to UNDER_REVIEW (to the agent).</li>
     *   <li>ON_HOLD — claim ON_HOLD.</li>
     * </ul>
     *
     * @throws IllegalStateException if there is no pending approval to decide
     */
    public String decide(SessionUser manager, long claimId, String decision, String remarks, String ip) {
        final String dec = decision == null ? "" : decision.trim().toUpperCase();
        if (!isValidDecision(dec)) {
            throw new IllegalArgumentException("Invalid approval decision.");
        }
        return Db.inTransaction(conn -> {
            Claim claim = claimDao.findById(conn, claimId);
            if (claim == null) {
                throw new IllegalStateException("Claim not found.");
            }
            Approval next = approvalDao.findNextPending(conn, claimId);
            if (next == null) {
                throw new IllegalStateException("This claim has no pending approval to decide.");
            }
            approvalDao.decide(conn, next.getId(), manager.getId(), dec, remarks);

            String newStatus;
            switch (dec) {
                case Approval.APPROVED:
                case Approval.CONDITIONAL:
                    newStatus = approvalDao.countPending(conn, claimId) > 0
                            ? ClaimStatus.PENDING_APPROVAL : ClaimStatus.APPROVED;
                    break;
                case Approval.REJECTED:
                    newStatus = ClaimStatus.REJECTED;
                    break;
                case Approval.RETURNED:
                    newStatus = ClaimStatus.UNDER_REVIEW;
                    break;
                default: // ON_HOLD
                    newStatus = ClaimStatus.ON_HOLD;
                    break;
            }
            claimDao.updateStatus(conn, claimId, newStatus);

            if (claim.getAgentId() != null) {
                notifications.notifyUser(conn, claim.getAgentId(), "ACTION",
                        "Claim " + claim.getClaimNo() + " was " + ClaimStatus.label(newStatus)
                        + " by " + manager.getFullName() + ".");
            }
            AuditLog a = new AuditLog();
            a.setUserId(manager.getId());
            a.setUsername(manager.getUsername());
            a.setRole(manager.getRole());
            a.setAction("APPROVAL_" + dec);
            a.setEntity(claim.getClaimNo() + " (" + next.getLevel() + ")");
            a.setIpAddress(ip);
            a.setResult(AuditLog.RESULT_SUCCESS);
            audit.record(conn, a);

            LOG.info("Manager {} decided {} on claim {} ({}) -> {}",
                    manager.getUsername(), dec, claim.getClaimNo(), next.getLevel(), newStatus);
            return newStatus;
        });
    }

    private static boolean isValidDecision(String dec) {
        return Approval.APPROVED.equals(dec) || Approval.REJECTED.equals(dec)
            || Approval.RETURNED.equals(dec) || Approval.ON_HOLD.equals(dec)
            || Approval.CONDITIONAL.equals(dec);
    }

    /**
     * Builds the approval chain for a forwarded claim within the caller's transaction.
     * @return number of pending approvals created (0 means fully approved at L1)
     */
    public int createForwardChain(Connection conn, long claimId, SessionUser agent, BigDecimal amount) {
        int requiredIdx = requiredLevelIndex(conn, amount); // 1..3

        // L1: the agent forwarding the claim has approved it.
        Approval l1 = new Approval();
        l1.setClaimId(claimId);
        l1.setLevel("L1");
        l1.setApproverId(agent.getId());
        l1.setApproverRole("AGENT");
        l1.setDecision(Approval.APPROVED);
        l1.setRemarks("Initial review complete; forwarded for approval.");
        l1.setDecidedAt(java.time.LocalDateTime.now());
        approvalDao.insert(conn, l1);

        int pending = 0;
        if (requiredIdx >= 2) {
            insertPending(conn, claimId, "L2", "MANAGER");
            pending++;
        }
        if (requiredIdx >= 3) {
            insertPending(conn, claimId, "L3", "DIRECTOR");
            pending++;
        }
        return pending;
    }

    private void insertPending(Connection conn, long claimId, String level, String role) {
        Approval a = new Approval();
        a.setClaimId(claimId);
        a.setLevel(level);
        a.setApproverRole(role);
        a.setDecision(Approval.PENDING);
        approvalDao.insert(conn, a);
    }

    /** Highest required level (1=L1,2=L2,3=L3) for an amount, per configured thresholds. */
    private int requiredLevelIndex(Connection conn, BigDecimal amount) {
        BigDecimal amt = amount == null ? BigDecimal.ZERO : amount;
        int idx = 1;
        for (ApprovalThreshold t : configDao.approvalThresholds(conn)) {
            boolean aboveMin = t.getMinAmount() == null || amt.compareTo(t.getMinAmount()) >= 0;
            if (aboveMin) {
                idx = levelIndex(t.getLevel());
            }
        }
        return idx;
    }

    private static int levelIndex(String level) {
        if ("L3".equals(level)) {
            return 3;
        }
        if ("L2".equals(level)) {
            return 2;
        }
        return 1;
    }
}

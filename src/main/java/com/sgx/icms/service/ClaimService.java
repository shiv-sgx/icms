package com.sgx.icms.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sgx.icms.dao.ClaimDao;
import com.sgx.icms.dao.DocumentDao;
import com.sgx.icms.dao.PolicyDao;
import com.sgx.icms.dao.PolicyholderDao;
import com.sgx.icms.db.Db;
import com.sgx.icms.domain.AuditLog;
import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.ClaimDocument;
import com.sgx.icms.domain.ClaimStatus;
import com.sgx.icms.domain.DocumentRequirement;
import com.sgx.icms.domain.Policy;
import com.sgx.icms.domain.Policyholder;
import com.sgx.icms.domain.Roles;
import com.sgx.icms.web.support.Paged;
import com.sgx.icms.web.support.SessionUser;
import com.sgx.icms.web.support.TimelineStage;

/**
 * Claim use-cases for the customer portal: resolve the customer's policyholder,
 * list/read their claims (ownership-enforced), create drafts/submissions (with
 * required-document seeding and agent notification), and withdraw.
 *
 * <p>Status transitions are guarded; multi-table writes run in one transaction.
 */
public class ClaimService {

    private static final Logger LOG = LoggerFactory.getLogger(ClaimService.class);

    private static final List<String> OPEN_STATUSES = Arrays.asList(
            ClaimStatus.DRAFT, ClaimStatus.SUBMITTED, ClaimStatus.UNDER_REVIEW,
            ClaimStatus.SURVEY_SCHEDULED, ClaimStatus.UNDER_ASSESSMENT, ClaimStatus.PENDING_APPROVAL,
            ClaimStatus.APPROVED, ClaimStatus.SETTLEMENT_PROCESSING, ClaimStatus.ON_HOLD);
    private static final List<String> SETTLED_STATUSES = Arrays.asList(
            ClaimStatus.SETTLED, ClaimStatus.CLOSED);

    private final ClaimDao claimDao = new ClaimDao();
    private final PolicyDao policyDao = new PolicyDao();
    private final PolicyholderDao policyholderDao = new PolicyholderDao();
    private final DocumentDao documentDao = new DocumentDao();
    private final AuditService audit = new AuditService();
    private final NotificationService notifications = new NotificationService();
    private final CommunicationService communications = new CommunicationService();

    /* ----------------------------- reads ----------------------------- */

    public Policyholder resolveCustomer(String email) {
        return Db.withConnection(conn -> policyholderDao.findByEmail(conn, email));
    }

    public List<Policy> policiesForCustomer(long policyholderId) {
        return Db.withConnection(conn -> policyDao.findByPolicyholder(conn, policyholderId));
    }

    public Paged<Claim> listForCustomer(long policyholderId, int page, int size) {
        int offset = (page - 1) * size;
        return Db.withConnection(conn -> {
            long total = claimDao.countByPolicyholder(conn, policyholderId);
            List<Claim> items = claimDao.findByPolicyholder(conn, policyholderId, size, offset);
            return new Paged<>(items, page, size, total);
        });
    }

    /** [total, open, settled] counts for the customer dashboard. */
    public long[] customerCounts(long policyholderId) {
        return Db.withConnection(conn -> new long[] {
                claimDao.countByPolicyholder(conn, policyholderId),
                claimDao.countByPolicyholderInStatuses(conn, policyholderId, OPEN_STATUSES),
                claimDao.countByPolicyholderInStatuses(conn, policyholderId, SETTLED_STATUSES)
        });
    }

    /** Returns the claim only if it belongs to this policyholder (authorisation). */
    public Claim getOwnedClaim(long policyholderId, long claimId) {
        Claim c = Db.withConnection(conn -> claimDao.findById(conn, claimId));
        if (c == null || c.getPolicyholderId() != policyholderId) {
            return null;
        }
        return c;
    }

    /* ----------------------------- writes ----------------------------- */

    /**
     * Creates a claim (draft or submitted) for the given policyholder. Seeds the
     * required-document checklist and, on submit, notifies agents — all atomically.
     *
     * @return the new claim id
     * @throws IllegalArgumentException if the chosen policy is not the customer's
     */
    public long createClaim(SessionUser actor, Policyholder ph, Claim draft, boolean submit, String ip) {
        final int year = LocalDate.now().getYear();
        return Db.inTransaction(conn -> {
            Policy policy = policyDao.findById(conn, draft.getPolicyId());
            if (policy == null || policy.getPolicyholderId() != ph.getId()) {
                throw new IllegalArgumentException("Please select one of your own policies.");
            }

            draft.setPolicyholderId(ph.getId());
            if (draft.getClaimantName() == null || draft.getClaimantName().trim().isEmpty()) {
                draft.setClaimantName(ph.getFullName());
            }
            draft.setClaimType(policy.getType()); // claim type follows the policy type
            draft.setStatus(submit ? ClaimStatus.SUBMITTED : ClaimStatus.DRAFT);
            draft.setRiskLevel("LOW");
            draft.setFraudScore(0);
            draft.setClaimNo(claimDao.nextClaimNo(conn, year));

            long claimId = claimDao.insert(conn, draft);
            seedRequiredDocuments(conn, claimId, draft.getClaimType(), draft.getClaimSubtype());

            String action = submit ? "CLAIM_SUBMITTED" : "CLAIM_DRAFT_SAVED";
            if (submit) {
                notifications.notifyRole(conn, Roles.AGENT, "ACTION",
                        "New claim " + draft.getClaimNo() + " submitted and awaiting acknowledgement.");
                communications.system(conn, claimId, "ICMS",
                        "Your claim " + draft.getClaimNo() + " has been received and is awaiting review.");
            }
            writeAudit(conn, actor, action, draft.getClaimNo(), ip);

            LOG.info("Customer {} {} claim {} (id={})", actor.getUsername(), action, draft.getClaimNo(), claimId);
            return claimId;
        });
    }

    /** Customer withdraws their own claim if its status permits. */
    public boolean withdraw(SessionUser actor, long policyholderId, long claimId, String ip) {
        return Db.inTransaction(conn -> {
            Claim c = claimDao.findById(conn, claimId);
            if (c == null || c.getPolicyholderId() != policyholderId) {
                return false; // not owner
            }
            if (!ClaimStatus.isWithdrawable(c.getStatus())) {
                throw new IllegalStateException("This claim can no longer be withdrawn.");
            }
            claimDao.updateStatus(conn, claimId, ClaimStatus.WITHDRAWN);
            notifications.notifyRole(conn, Roles.AGENT, "INFO",
                    "Claim " + c.getClaimNo() + " was withdrawn by the customer.");
            writeAudit(conn, actor, "CLAIM_WITHDRAWN", c.getClaimNo(), ip);
            return true;
        });
    }

    /* ----------------------------- helpers ----------------------------- */

    private void seedRequiredDocuments(java.sql.Connection conn, long claimId, String type, String subtype) {
        List<DocumentRequirement> reqs = documentDao.findRequirements(conn, type, subtype);
        for (DocumentRequirement r : reqs) {
            ClaimDocument d = new ClaimDocument();
            d.setClaimId(claimId);
            d.setDocType(r.getDocType());
            d.setUploadStatus(r.isRequired() ? "PENDING" : "CONDITIONAL");
            d.setVerificationStatus("PENDING");
            documentDao.insert(conn, d);
        }
    }

    private void writeAudit(java.sql.Connection conn, SessionUser actor, String action, String claimNo, String ip) {
        AuditLog a = new AuditLog();
        a.setUserId(actor.getId());
        a.setUsername(actor.getUsername());
        a.setRole(actor.getRole());
        a.setAction(action);
        a.setEntity(claimNo);
        a.setIpAddress(ip);
        a.setResult(AuditLog.RESULT_SUCCESS);
        audit.record(conn, a);
    }

    /** Builds the status timeline view model for a claim. */
    public List<TimelineStage> timeline(Claim c) {
        List<TimelineStage> stages = new ArrayList<>();
        String status = c.getStatus();

        if (ClaimStatus.DRAFT.equals(status)) {
            stages.add(new TimelineStage("Draft", TimelineStage.CURRENT));
            for (String s : ClaimStatus.LIFECYCLE) {
                stages.add(new TimelineStage(ClaimStatus.label(s), TimelineStage.PENDING));
            }
            return stages;
        }

        int curIdx;
        if (ClaimStatus.ON_HOLD.equals(status)) {
            curIdx = ClaimStatus.LIFECYCLE.indexOf(ClaimStatus.PENDING_APPROVAL);
        } else if (ClaimStatus.REJECTED.equals(status) || ClaimStatus.WITHDRAWN.equals(status)) {
            curIdx = -1; // off-path terminal; the status pill conveys the real state
        } else {
            curIdx = ClaimStatus.lifecycleIndex(status);
        }

        for (int i = 0; i < ClaimStatus.LIFECYCLE.size(); i++) {
            String state;
            if (curIdx < 0) {
                state = TimelineStage.PENDING;
            } else if (i < curIdx) {
                state = TimelineStage.DONE;
            } else if (i == curIdx) {
                state = TimelineStage.CURRENT;
            } else {
                state = TimelineStage.PENDING;
            }
            stages.add(new TimelineStage(ClaimStatus.label(ClaimStatus.LIFECYCLE.get(i)), state));
        }
        return stages;
    }
}

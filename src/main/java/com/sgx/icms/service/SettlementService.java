package com.sgx.icms.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sgx.icms.dao.ClaimDao;
import com.sgx.icms.dao.SettlementDao;
import com.sgx.icms.db.Db;
import com.sgx.icms.domain.AuditLog;
import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.ClaimStatus;
import com.sgx.icms.domain.Settlement;
import com.sgx.icms.web.support.SessionUser;

/**
 * Settlement authorisation and payment tracking. Authorising creates the settlement
 * and moves the claim into SETTLEMENT_PROCESSING; advancing walks the payment tracker
 * and syncs the claim to SETTLED / CLOSED at the right points.
 */
public class SettlementService {

    private static final Logger LOG = LoggerFactory.getLogger(SettlementService.class);

    private final SettlementDao settlementDao = new SettlementDao();
    private final ClaimDao claimDao = new ClaimDao();
    private final CommunicationService communications = new CommunicationService();
    private final AuditService audit = new AuditService();

    public Settlement forClaim(long claimId) {
        return Db.withConnection(conn -> settlementDao.findByClaim(conn, claimId));
    }

    /** Authorise (or re-amount) a settlement. Requires an approved/processing claim. */
    public void authorize(SessionUser actor, long claimId, BigDecimal amount, String paymentMethod,
                          String accountHolder, String bankName, String accountNumber, String ifsc,
                          String justification, String ip) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Enter a valid settlement amount.");
        }
        Db.inTransaction(conn -> {
            Claim claim = claimDao.findById(conn, claimId);
            if (claim == null) {
                throw new IllegalStateException("Claim not found.");
            }
            Settlement existing = settlementDao.findByClaim(conn, claimId);
            if (existing == null) {
                if (!ClaimStatus.APPROVED.equals(claim.getStatus())) {
                    throw new IllegalStateException("Only approved claims can be settled.");
                }
                Settlement s = new Settlement();
                s.setClaimId(claimId);
                s.setFinalAmount(amount);
                s.setJustification(justification);
                s.setPaymentMethod(paymentMethod);
                s.setAccountHolder(accountHolder);
                s.setBankName(bankName);
                s.setAccountNumber(accountNumber);
                s.setIfscCode(ifsc);
                s.setStatus(Settlement.AUTHORIZED);
                s.setApprovedBy(actor.getId());
                settlementDao.insert(conn, s);
                claimDao.updateStatus(conn, claimId, ClaimStatus.SETTLEMENT_PROCESSING);
                communications.system(conn, claimId, "ICMS",
                        "Settlement of ₹ " + amount + " has been authorised for your claim.");
            } else {
                settlementDao.updateAmount(conn, claimId, amount, justification);
            }
            writeAudit(conn, actor, "SETTLEMENT_AUTHORIZED", claim.getClaimNo() + " (₹" + amount + ")", ip);
            LOG.info("{} authorised settlement {} for claim {}", actor.getUsername(), amount, claim.getClaimNo());
            return null;
        });
    }

    /** Advance the payment tracker one step; returns the new status (or current if at end). */
    public String advance(SessionUser actor, long claimId, String ip) {
        return Db.inTransaction(conn -> {
            Claim claim = claimDao.findById(conn, claimId);
            Settlement s = settlementDao.findByClaim(conn, claimId);
            if (claim == null || s == null) {
                throw new IllegalStateException("No settlement to advance.");
            }
            int idx = Settlement.TRACKER.indexOf(s.getStatus());
            if (idx < 0 || idx >= Settlement.TRACKER.size() - 1) {
                return s.getStatus(); // already closed / unknown
            }
            String next = Settlement.TRACKER.get(idx + 1);
            settlementDao.advance(conn, claimId, next);

            if (Settlement.PAYMENT_CONFIRMED.equals(next)) {
                claimDao.updateStatus(conn, claimId, ClaimStatus.SETTLED);
                communications.system(conn, claimId, "ICMS",
                        "Your claim has been settled. Payment of ₹ " + s.getFinalAmount() + " is confirmed.");
            } else if (Settlement.CLOSED.equals(next)) {
                claimDao.updateStatus(conn, claimId, ClaimStatus.CLOSED);
            }
            writeAudit(conn, actor, "SETTLEMENT_" + next, claim.getClaimNo(), ip);
            return next;
        });
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

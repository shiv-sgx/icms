package com.sgx.icms.service;

import java.math.BigDecimal;

import com.sgx.icms.dao.ClaimDao;
import com.sgx.icms.dao.SettlementDao;
import com.sgx.icms.db.Db;
import com.sgx.icms.domain.AuditLog;
import com.sgx.icms.domain.Settlement;
import com.sgx.icms.web.support.SessionUser;

/** Manager-specific dashboard stats and settlement override. */
public class ManagerService {

    private final SettlementDao settlementDao = new SettlementDao();
    private final ClaimDao claimDao = new ClaimDao();
    private final AuditService audit = new AuditService();

    /** [pendingApproval, highRisk, slaBreaches, settled] for the manager dashboard. */
    public long[] dashboardStats() {
        return Db.withConnection(conn -> new long[] {
            Db.queryLong(conn, "SELECT COUNT(*) FROM claims WHERE status = 'PENDING_APPROVAL'"),
            Db.queryLong(conn, "SELECT COUNT(*) FROM claims WHERE risk_level = 'HIGH' "
                    + "AND status NOT IN ('SETTLED','CLOSED','REJECTED','WITHDRAWN')"),
            Db.queryLong(conn, "SELECT COUNT(*) FROM claims WHERE sla_due_date < CURDATE() "
                    + "AND status NOT IN ('SETTLED','CLOSED','REJECTED','WITHDRAWN')"),
            Db.queryLong(conn, "SELECT COUNT(*) FROM claims WHERE status IN ('SETTLED','CLOSED')")
        });
    }

    /**
     * Manager overrides the settlement amount (must already exist, created by the agent
     * post-approval).
     * @throws IllegalStateException if there is no settlement to override
     */
    public void overrideSettlement(SessionUser manager, long claimId, BigDecimal amount,
                                   String justification, String ip) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Enter a valid override amount.");
        }
        Db.inTransaction(conn -> {
            Settlement s = settlementDao.findByClaim(conn, claimId);
            if (s == null) {
                throw new IllegalStateException("No settlement exists yet to override.");
            }
            settlementDao.updateAmount(conn, claimId, amount, justification);
            com.sgx.icms.domain.Claim claim = claimDao.findById(conn, claimId);
            AuditLog a = new AuditLog();
            a.setUserId(manager.getId());
            a.setUsername(manager.getUsername());
            a.setRole(manager.getRole());
            a.setAction("SETTLEMENT_OVERRIDE");
            a.setEntity((claim == null ? ("claim:" + claimId) : claim.getClaimNo()) + " (₹" + amount + ")");
            a.setIpAddress(ip);
            a.setResult(AuditLog.RESULT_SUCCESS);
            audit.record(conn, a);
            return null;
        });
    }
}
